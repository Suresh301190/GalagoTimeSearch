// BSD License (http://www.galagosearch.org/license)

package org.galagosearch.tupleflow.typebuilder;

import java.io.IOException;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

/**
 *
 * @author trevor
 */
public class ParserDriver {
    public static TypeSpecification getTypeSpecification(String fileName) throws IOException, RecognitionException {
        ANTLRFileStream input = new ANTLRFileStream(fileName);
        GalagoTypeBuilderLexer lexer = new GalagoTypeBuilderLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GalagoTypeBuilderParser parser = new GalagoTypeBuilderParser(tokens);
        TypeSpecification spec = parser.type_def();
        return spec;
    }
}
