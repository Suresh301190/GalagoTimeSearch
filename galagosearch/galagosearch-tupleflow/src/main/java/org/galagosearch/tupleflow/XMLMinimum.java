// BSD License (http://www.galagosearch.org/license)

package org.galagosearch.tupleflow;

import java.io.IOException;
import org.galagosearch.tupleflow.execution.Verified;
import org.galagosearch.tupleflow.types.XMLFragment;

/**
 *
 * @author trevor
 */

@InputClass(className="org.galagosearch.tupleflow.types.XMLFragment")
@OutputClass(className="org.galagosearch.tupleflow.types.XMLFragment", order={"+nodePath"})
@Verified
public class XMLMinimum extends StandardStep<XMLFragment, XMLFragment> {
    double minimum = 0;
    String key = "";
    
    public void process(XMLFragment fragment) {
        if(key == null) 
            key = fragment.nodePath;
        minimum = Math.min(minimum, Double.parseDouble(fragment.innerText));
        System.err.println("XMLMinimum: " + minimum);
    }
    
    public void close() throws IOException {
        processor.process(new XMLFragment(key, Double.toString(minimum)));
        super.close();
    }
}
