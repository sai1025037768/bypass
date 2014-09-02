package in.uncod.android.bypass;

import android.util.TypedValue;
import in.uncod.android.bypass.Element.Type;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;

public class Bypass {
	static {
		System.loadLibrary("bypass");
	}

	private final Options mOptions;

	private final int mListItemIndent;
	private final int mBlockQuoteIndent;
	private final int mCodeBlockIndent;

	/**
	 * @deprecated Use {@link #Bypass(android.content.Context)} instead.
	 */
	@Deprecated
	public Bypass() {
		// Default constructor for backwards-compatibility
		mOptions = new Options();
		mListItemIndent = 20;
		mBlockQuoteIndent = 10;
		mCodeBlockIndent = 10;
	}

	public Bypass(Context context) {
		this(context, new Options());
	}

	public Bypass(Context context, Options options) {
		mOptions = options;

		mListItemIndent = (int) TypedValue.applyDimension(mOptions.mListItemIndentUnit,
			mOptions.mListItemIndentSize,
			context.getResources().getDisplayMetrics());

		mBlockQuoteIndent = (int) TypedValue.applyDimension(mOptions.mBlockQuoteIndentUnit,
			mOptions.mBlockQuoteIndentSize,
			context.getResources().getDisplayMetrics());

		mCodeBlockIndent = (int) TypedValue.applyDimension(mOptions.mCodeBlockIndentUnit,
			mOptions.mCodeBlockIndentSize,
			context.getResources().getDisplayMetrics());
	}

	public CharSequence markdownToSpannable(String markdown) {
		Document document = processMarkdown(markdown);

		CharSequence[] spans = new CharSequence[document.getElementCount()];
		for (int i = 0; i < document.getElementCount(); i++) {
			spans[i] = recurseElement(document.getElement(i));
		}

		return TextUtils.concat(spans);
	}

	private native Document processMarkdown(String markdown);

	private CharSequence recurseElement(Element element) {
		Type type = element.getType();

		CharSequence[] spans = new CharSequence[element.size()];
		for (int i = 0; i < element.size(); i++) {
			spans[i] = recurseElement(element.children[i]);
		}

		CharSequence concat = TextUtils.concat(spans);

		SpannableStringBuilder builder = new SpannableStringBuilder();

		String text = element.getText();
		if (element.size() == 0
				&& element.getParent() != null
                && element.getParent().getType() != Type.BLOCK_CODE) {
			text = text.replace('\n', ' ');
		}

		switch (type) {
			case LIST:
				if (element.getParent() != null
					&& element.getParent().getType() == Type.LIST_ITEM) {
					builder.append("\n");
				}
				break;
			case LINEBREAK:
				builder.append("\n");
				break;
			case LIST_ITEM:
				builder.append(mOptions.mListItem);
				break;
			case AUTOLINK:
				builder.append(element.getAttribute("link"));
				break;
		}

		builder.append(text);
		builder.append(concat);

		if (element.isBlockElement() && type != Type.LIST_ITEM) {
			builder.append("\n");
		}

		switch(type) {
			case HEADER:
				String levelStr = element.getAttribute("level");
				int level = Integer.parseInt(levelStr);
				setSpan(builder, new RelativeSizeSpan(mOptions.mHeaderSizes[level - 1]));
				setSpan(builder, new StyleSpan(Typeface.BOLD));
				break;
			case LIST_ITEM:
				if (element.getParent().getParent() != null) {
					setSpan(builder, new LeadingMarginSpan.Standard(mListItemIndent));
				}
				break;
			case EMPHASIS:
				setSpan(builder, new StyleSpan(Typeface.ITALIC));
				break;
			case DOUBLE_EMPHASIS:
				setSpan(builder, new StyleSpan(Typeface.BOLD));
				break;
			case TRIPLE_EMPHASIS:
				setSpan(builder, new StyleSpan(Typeface.BOLD_ITALIC));
				break;
			case BLOCK_CODE:
				setSpan(builder, new LeadingMarginSpan.Standard(mCodeBlockIndent));
				setSpan(builder, new TypefaceSpan("monospace"));
				break;
			case CODE_SPAN:
				setSpan(builder, new TypefaceSpan("monospace"));
				break;
			case LINK:
			case AUTOLINK:
				setSpan(builder, new URLSpan(element.getAttribute("link")));
				break;
			case BLOCK_QUOTE:
				setSpan(builder, new QuoteSpan(mOptions.mBlockQuoteColor));
				setSpan(builder, new LeadingMarginSpan.Standard(mBlockQuoteIndent));
				setSpan(builder, new StyleSpan(Typeface.ITALIC));
				break;
			case STRIKETHROUGH:
				setSpan(builder, new StrikethroughSpan());
				break;
		}

		return builder;
	}

	private static void setSpan(SpannableStringBuilder builder, Object what) {
		builder.setSpan(what, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	/**
	 * Configurable options for how Bypass renders certain elements.
	 */
	public static final class Options {
		private float[] mHeaderSizes;

		private String mListItem;
		private int mListItemIndentUnit;
		private float mListItemIndentSize;

		private int mBlockQuoteColor;
		private int mBlockQuoteIndentUnit;
		private float mBlockQuoteIndentSize;

		private int mCodeBlockIndentUnit;
		private float mCodeBlockIndentSize;

		public Options() {
			mHeaderSizes = new float[] {
				1.5f, // h1
				1.4f, // h2
				1.3f, // h3
				1.2f, // h4
				1.1f, // h5
				1.0f, // h6
			};

			mListItem = "\u2022";
			mListItemIndentUnit = TypedValue.COMPLEX_UNIT_DIP;
			mListItemIndentSize = 10;

			mBlockQuoteColor = 0xff0000ff;
			mBlockQuoteIndentUnit = TypedValue.COMPLEX_UNIT_DIP;
			mBlockQuoteIndentSize = 10;

			mCodeBlockIndentUnit = TypedValue.COMPLEX_UNIT_DIP;
			mCodeBlockIndentSize = 10;
		}

		public Options setHeaderSizes(float[] headerSizes) {
			if (headerSizes == null) {
				throw new IllegalArgumentException("headerSizes must not be null");
			}
			else if (headerSizes.length != 6) {
				throw new IllegalArgumentException("headerSizes must have 6 elements (h1 through h6)");
			}

			mHeaderSizes = headerSizes;

			return this;
		}

		public Options setListItem(String listItem) {
			mListItem = listItem;
			return this;
		}

		public Options setListItemIndentSize(int unit, float size) {
			mListItemIndentUnit = unit;
			mListItemIndentSize = size;
			return this;
		}

		public Options setBlockQuoteColor(int color) {
			mBlockQuoteColor = color;
			return this;
		}

		public Options setBlockQuoteIndentSize(int unit, float size) {
			mBlockQuoteIndentUnit = unit;
			mBlockQuoteIndentSize = size;
			return this;
		}

		public Options setCodeBlockIndentSize(int unit, float size) {
			mCodeBlockIndentUnit = unit;
			mCodeBlockIndentSize = size;
			return this;
		}
	}
}
