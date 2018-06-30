package in.lstn;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.Iterables;

import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import in.lstn.Podcasts.InputConverterFn;
import in.lstn.vo.InputPodcastVO;

/**
 * Tests of Podcasts
 */
@RunWith(JUnit4.class)
public class PodcastsTest {

  public static final ArrayList<String> INPUT = new ArrayList<String>(Arrays.asList(
    "[",
    "{\"category\": \"Comedy\", \"feedUrl\": \"http://feeds.feedburner.com/smalldoseswithamandaseales\", \"genres\": [\"Comedy\", \"Podcasts\", \"Health\", \"Self-Help\"], \"name\": \"Small Doses\", \"countryCode\": \"CA\", \"isPopular\": true},",
    "{\"category\": \"Business\", \"feedUrl\": \"http://optionalpha.libsyn.com/rss\", \"genres\": [\"Investing\", \"Podcasts\", \"Business\"], \"name\": \"The Option Alpha Podcast: Options Trading | Stock Options | Stock Trading | Trading Online\", \"countryCode\": \"CA\", \"isPopular\": true},",
    "{\"category\": \"Technology\", \"feedUrl\": \"http://feeds.feedburner.com/EpicenterBitcoin\", \"genres\": [\"Tech News\", \"Podcasts\", \"Technology\"], \"name\": \"Epicenter \\u2013 Podcast on Blockchain, Ethereum, Bitcoin and Distributed Technologies\", \"countryCode\": \"CA\", \"isPopular\": true},",
    "{\"category\": \"Comedy\", \"feedUrl\": \"http://feeds.feedburner.com/smalldoseswithamandaseales\", \"genres\": [\"Comedy\", \"Podcasts\", \"Health\", \"Self-Help\"], \"name\": \"Small Doses\", \"countryCode\": \"US\", \"isPopular\": true},",
    "{\"category\": \"Business\", \"feedUrl\": \"http://optionalpha.libsyn.com/rss\", \"genres\": [\"Investing\", \"Podcasts\", \"Business\"], \"name\": \"The Option Alpha Podcast: Options Trading | Stock Options | Stock Trading | Trading Online\", \"countryCode\": \"US\", \"isPopular\": false},",
    "{\"category\": \"Technology\", \"feedUrl\": \"http://feeds.feedburner.com/EpicenterBitcoin\", \"genres\": [\"Tech News\", \"Podcasts\", \"Technology\"], \"name\": \"Epicenter \\u2013 Podcast on Blockchain, Ethereum, Bitcoin and Distributed Technologies\", \"countryCode\": \"US\", \"isPopular\": true},",
    "]"
  ));

  @Rule
  public TestPipeline p = TestPipeline.create();

  @Test
  @Category(ValidatesRunner.class)
  public void testInputConversion() throws Exception {
    
    PCollection<String> lines = p.apply(Create.of(INPUT).withCoder(StringUtf8Coder.of()));

    PCollection<KV<String, InputPodcastVO>> vos = lines.apply(
        ParDo.of(new InputConverterFn()));
    
    PAssert.that(vos).satisfies(contents -> {
      assertThat(Iterables.isEmpty(contents), is(false));
      assertThat(Iterables.size(contents), is(6));
      return null; // no problemo.
    });

    PAssert.thatMultimap(vos).satisfies(contents -> {
      assertThat(contents.keySet().contains("http://optionalpha.libsyn.com/rss"), is(true));
      assertThat(contents.keySet().contains("http://feeds.feedburner.com/smalldoseswithamandaseales"), is(true));
      assertThat(contents.keySet().contains("http://feeds.feedburner.com/EpicenterBitcoin"), is(true));

      Iterable<InputPodcastVO> bitcoin = contents.get("http://feeds.feedburner.com/EpicenterBitcoin");
      assertThat(Iterables.size(bitcoin), is(2));
      InputPodcastVO[] items = Iterables.toArray(bitcoin, InputPodcastVO.class);
      assertThat(items[0].countryCode, not(items[1].countryCode));

      Iterable<InputPodcastVO> alpha = contents.get("http://optionalpha.libsyn.com/rss");
      assertThat(Iterables.size(alpha), is(2));
      items = null;
      items = Iterables.toArray(alpha, InputPodcastVO.class);
      assertThat(items[0].countryCode, not(items[1].countryCode));
      assertThat(items[0].isPopular, not(items[1].isPopular));
      return null;
    });
    
    p.run().waitUntilFinish();
  }
}
