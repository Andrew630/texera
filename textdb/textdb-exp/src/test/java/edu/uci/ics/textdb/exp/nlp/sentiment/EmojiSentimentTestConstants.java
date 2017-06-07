package edu.uci.ics.textdb.exp.nlp.sentiment;

import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.schema.Attribute;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;

/**
 * Created by Vinay on 23-05-2017.
 */
public class EmojiSentimentTestConstants {
    public static String TEXT = "text";

    public static Attribute TEXT_ATTRIBUTE = new Attribute("text", AttributeType.TEXT);

    public static Schema SENTIMENT_SCHEMA = new Schema(TEXT_ATTRIBUTE);

    public static Tuple POSITIVE_TUPLE1 = new Tuple(SENTIMENT_SCHEMA,
            new TextField("Programming is so super awesome. :) "));
    public static Tuple POSITIVE_TUPLE2 = new Tuple(SENTIMENT_SCHEMA,
            new TextField("Programming is so super awesome. \uD83D\uDE00"));

    public static Tuple NEUTRAL_TUPLE = new Tuple(SENTIMENT_SCHEMA,
            new TextField("TextDB uses Java."));

    public static Tuple NEGATIVE_TUPLE1 = new Tuple(SENTIMENT_SCHEMA,
            new TextField("Bugs are always annoying. :("));
    public static Tuple NEGATIVE_TUPLE2 = new Tuple(SENTIMENT_SCHEMA,
            new TextField("Bugs are always annoying.  \uD83D\uDE41"));
}
