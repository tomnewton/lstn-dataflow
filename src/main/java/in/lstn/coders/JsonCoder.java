package in.lstn.coders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.coders.CustomCoder;
import org.apache.beam.sdk.values.TypeDescriptor;

public class JsonCoder<T> extends CustomCoder<T> {
    private static final long serialVersionUID = -3265818032419610814L;

	public static <T> JsonCoder<T> of(Class<T> clazz) {
        return new JsonCoder<>(clazz);
    }

    public static <T> JsonCoder<T> of (TypeDescriptor<T> type) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) type.getRawType();
        return of(clazz);
    }

    transient private Gson gson;
    private Class<T> type;
    private TypeDescriptor<T> typeDescriptor;

    protected JsonCoder(Class<T> clazz) {
        this.type = clazz;
        this.typeDescriptor = TypeDescriptor.of(type);
        this.gson = new GsonBuilder().setLenient().create();
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public TypeDescriptor<T> getEncodedTypeDescriptor() {
        return typeDescriptor;
    }

    //@SuppressWarnings("unused")
    /*public static CoderProvider getCoderProvider() {
      return new JsonCoderProvider();
    }
 

    static class JsonCoderProvider extends CoderProvider {
      @Override
      public <T> Coder<T> coderFor(TypeDescriptor<T> typeDescriptor,
          List<? extends Coder<?>> componentCoders) throws CannotProvideCoderException {
          return JsonCoder.of(typeDescriptor);
      }
    }*/

	@Override
	public void encode(T value, OutputStream outStream) throws CoderException, IOException {
        if ( value != null) {
            String json = gson.toJson(value);
            outStream.write(json.getBytes(StandardCharsets.UTF_8));
        }
	}

	@Override
	public T decode(InputStream inStream) throws CoderException, IOException {
        Reader reader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        if ( reader.ready() ) {        
            T decoded = gson.fromJson(reader, this.type);
            return decoded;
        }
        return null;
	}

	@Override
    public void verifyDeterministic() throws NonDeterministicException {}
    
    
}