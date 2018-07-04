package in.lstn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import in.lstn.pubsub.messages.PodcastMessages;
import in.lstn.vo.InputPodcastVO;
import in.lstn.vo.OutputPodcastVO;
import in.lstn.vo.OutputPodcastVO.CountryInfoVO;

public class Podcasts {
    
    static class InputConverterFn extends DoFn<String, KV<String, InputPodcastVO>> {
        private static final long serialVersionUID = 1L;
		private final Counter lineCounter = Metrics.counter(InputConverterFn.class, "linesProcessed");

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<KV<String, InputPodcastVO>> receiver) {
            String trimmed = element.trim();

            if ( trimmed.endsWith("}") == false) {
                int index = trimmed.lastIndexOf("}");
                trimmed = trimmed.substring(0, index+1);
            }
        
            lineCounter.inc();
            
            Gson gson = new Gson();
            // Deserialise the input string
            InputPodcastVO vo = gson.fromJson(trimmed, InputPodcastVO.class);
            if ( vo != null ) {
                // Create output.
                KV<String, InputPodcastVO> out = KV.of(vo.feedUrl, vo);
                
                // Output each vo into the output PCollection.
                receiver.output(out);
            }
        }
    }

    public static class PrepOutput extends DoFn<KV<String, Iterable<InputPodcastVO>>, OutputPodcastVO> {

        private static final long serialVersionUID = 1L;
        
        @ProcessElement
        public void processElement(@Element  KV<String, Iterable<InputPodcastVO>> element, OutputReceiver<OutputPodcastVO> receiver) {

            OutputPodcastVO out = new OutputPodcastVO();
            
            Map<String, Boolean> genres = new HashMap<String, Boolean>();
            Map<String, CountryInfoVO> countryInfo = new HashMap<String, CountryInfoVO>();

            InputPodcastVO latest = null;
            Iterator<InputPodcastVO> iter = element.getValue().iterator();
            while(iter.hasNext()) {
                latest = iter.next();
                // add genres
                for (int i = 0; i < latest.genres.length; i++ ){
                    genres.put(latest.genres[i], true);
                }

                countryInfo.put(latest.countryCode, new CountryInfoVO(true, latest.isPopular));
            }

            if ( latest == null ) {
                return;
            }

            String feedUrl = element.getKey();
            
            out.feedUrl = feedUrl;
            out.category = latest.category;
            out.name = latest.name;

            // now the keys of genres
            out.genres = genres.keySet().toArray(new String[0]);

            // now create the CountryInfo 
            out.countryInfo = countryInfo;

            receiver.output(out);
        }
    }

    public static class ConvertToPubsubMessage extends SimpleFunction<OutputPodcastVO, PubsubMessage> {

        private static final long serialVersionUID = 1L;

        @Override
        public PubsubMessage apply(OutputPodcastVO out) {
            PodcastMessages.PodcastDirectoryUpdate.Builder update = 
                PodcastMessages.PodcastDirectoryUpdate.newBuilder()
                .setCategory(out.category)
                .setFeedUrl(out.feedUrl)
                .setName(out.name);

            for ( Map.Entry<String, in.lstn.vo.OutputPodcastVO.CountryInfoVO> entry: out.countryInfo.entrySet() ){
                PodcastMessages.PodcastDirectoryUpdate.CountryInfo nfo = 
                    PodcastMessages.PodcastDirectoryUpdate.CountryInfo.newBuilder()
                    .setIsPopular(entry.getValue().isPopular)
                    .setIsPublished(entry.getValue().isPublished).build();
                
                update.putCountryInfo(entry.getKey(), nfo);
            }

            return new PubsubMessage(update.build().toByteArray(), null);
        }
    }

    public static class MergePodcasts extends PTransform<PCollection<String>, PCollection<PubsubMessage>> {
        private static final long serialVersionUID = 1L;

        @Override
        public PCollection<PubsubMessage> expand(PCollection<String> lines) {

            // Convert lines of text into InputPodcastVOs, keyed on their feedUrls.
            PCollection<KV<String, InputPodcastVO>> vos = lines.apply(
                ParDo.of(new InputConverterFn()));

            // Group the InputPodcastVOs by their feed url.
            PCollection<KV<String, Iterable<InputPodcastVO>>> merged = vos.apply(GroupByKey.create());

            // Convert to OutputFormat
            PCollection<OutputPodcastVO> output = merged.apply(ParDo.of(new PrepOutput()));
            
            // out as pubsub messages.
            PCollection<PubsubMessage> messages = output.apply(MapElements.via(new ConvertToPubsubMessage()));

            return messages;
        }
    }

    public interface PodcastOptions extends PipelineOptions {
        /**
         * By default, this example reads from a public dataset containing the text of
         * King Lear. Set this option to choose a different input file or glob.
         */
        @Description("Path of the file to read from")
        @Default.String("gs://spider-dumps/latest/*")
        ValueProvider<String> getInputFile();
        void setInputFile(ValueProvider<String> value);

        /**
         * Set this required option to specify which Pubsub Topic to write to...
         */
        @Description("Topic to write the results to...")
        @Default.String("projects/lstn-in-dev/topics/directory-update")
        ValueProvider<String> getOutputTopic();
        void setOutputTopic(ValueProvider<String> value);
    }

    static void runPodcasts(PodcastOptions options) {
        Pipeline p = Pipeline.create(options);

        p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
        .apply("Merge Podcast Records", new MergePodcasts())
        .apply("Pubsub", PubsubIO.writeMessages().to(options.getOutputTopic()));
    
        p.run();
      }
    
      public static void main(String[] args) {
        PodcastOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
          .as(PodcastOptions.class);
    
        runPodcasts(options);
      }
}