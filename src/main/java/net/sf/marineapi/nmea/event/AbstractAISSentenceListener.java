package net.sf.marineapi.nmea.event;

import net.sf.marineapi.ais.parser.AISMessageFactory;
import net.sf.marineapi.ais.sentence.AISMessage;
import net.sf.marineapi.nmea.sentence.AISSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Abstract base listener for AIS messages. Extend this class to create listener
 * for specific AIS message type.
 *
 * @author Kimmo Tuukkanen
 */
public abstract class AbstractAISSentenceListener<T extends AISMessage>
		implements SentenceListener {

	private final Type expectedMessageType;
	private Queue<AISSentence> queue = new LinkedList<AISSentence>();
	private final AISMessageFactory factory = AISMessageFactory.getInstance();

	/**
	 * Constructor
	 */
	public AbstractAISSentenceListener() {
		// TODO: not DRY
		ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
		Type[] superClassTypeArgs = superClass.getActualTypeArguments();
		this.expectedMessageType = superClassTypeArgs[0];
	}

	@Override
	public void sentenceRead(SentenceEvent event) {
		Sentence s = event.getSentence();
		if (s instanceof AISSentence) {
			preParse((AISSentence) s);
		}
	}

	/**
	 * Concatenate and pre-parse AIS sentences/messages.
	 */
	private void preParse(AISSentence sentence) {

		System.out.println("sentenceRead: " + sentence.getSentenceId());

		if (sentence.isFirstFragment()) {
			queue.clear();
		}

		queue.add(sentence);

		if (sentence.isLastFragment()) {

			AISSentence[] sentences = queue.toArray(new AISSentence[queue.size()]);
			AISMessage message = factory.create(sentences);

			if (message != null) {
				Class<?>[] interfaces = message.getClass().getInterfaces();
				if (Arrays.asList(interfaces).contains(expectedMessageType)) {
					onMessage((T) message);
				}
				System.out.println("message type: " + message.getMessageType());
				System.out.println("message interface: " + interfaces[0]);
			}
		}
	}

	/**
	 * Invoked when AIS message has been parsed.
	 *
	 * @param msg AISMessage of subtype <code>T</code>.
	 */
	public abstract void onMessage(T msg);

	/**
	 * Empty implementation.
	 *
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	@Override
	public void readingPaused() {
	}

	/**
	 * Empty implementation.
	 *
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	@Override
	public void readingStarted() {
	}

	/**
	 * Empty implementation.
	 *
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	@Override
	public void readingStopped() {
	}
}