#ifndef _BYPASS_PARSER_H_
#define _BYPASS_PARSER_H_

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <cstdio>
#include <cstdlib>
#include <map>
#include <boost/algorithm/string.hpp>

extern "C" {
#include "markdown.h"
}

#include "document.h"
#include "element.h"

#define INPUT_UNIT 1024
#define OUTPUT_UNIT 64

namespace Bypass {

	class Parser {
	public:
		Parser();
		~Parser();

		Document parse(const char* markdown);
		Document parse(const std::string &markdown);

		// Block Element Callbacks

		void parsedBlockCode(struct buf *ob, struct buf *text);
		void parsedBlockQuote(struct buf *ob, struct buf *text);
		void parsedHeader(struct buf *ob, struct buf *text, int level);
		void parsedList(struct buf *ob, struct buf *text, int flags);
		void parsedListItem(struct buf *ob, struct buf *text, int flags);
		void parsedParagraph(struct buf *ob, struct buf *text);

		// Span Element Callbacks

		int parsedCodeSpan(struct buf *ob, struct buf *text);
		int parsedDoubleEmphasis(struct buf *ob, struct buf *text, char c);
		int parsedEmphasis(struct buf *ob, struct buf *text, char c);
		int parsedTripleEmphasis(struct buf *ob, struct buf *text, char c);
		int parsedLinebreak(struct buf *ob);
		int parsedLink(struct buf *ob, struct buf *link, struct buf *title, struct buf *content);

		// Low Level Callbacks

		void parsedNormalText(struct buf *ob, struct buf *text);

		// Debugging

		void printBuf(struct buf *b);

	private:
		Document document;
		std::map<int, Element> elementSoup;
		int elementCount;
		void handleBlock(Type, struct buf *ob, struct buf *text, int extra = -1);
		void handleSpan(Type, struct buf *ob, struct buf *text, struct buf *extra = NULL, struct buf *extra2 = NULL);
		void createSpan(Element, struct buf *ob);
		void eraseTrailingControlCharacters(std::string controlCharacters);
	};

}

#endif // _BYPASS_PARSER_H_