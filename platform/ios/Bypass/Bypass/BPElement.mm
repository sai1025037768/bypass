//
//  BPElement.m
//  Bypass
//
//  Created by Damian Carrillo on 2/28/13.
//  Copyright (c) 2013 Uncodin. All rights reserved.
//

#import "BPElementPrivate.h"

// Block Element Types

const BPElementType BPBlockCode      = Bypass::BLOCK_CODE;
const BPElementType BPBlockQuote     = Bypass::BLOCK_QUOTE;
const BPElementType BPBlockHTML      = Bypass::BLOCK_HTML;
const BPElementType BPHeader         = Bypass::HEADER;
const BPElementType BPHRule          = Bypass::HRULE;
const BPElementType BPList           = Bypass::LIST;
const BPElementType BPListItem       = Bypass::LIST_ITEM;
const BPElementType BPParagraph      = Bypass::PARAGRAPH;
const BPElementType BPTable          = Bypass::TABLE;
const BPElementType BPTableCell      = Bypass::TABLE_CELL;
const BPElementType BPTableRow       = Bypass::TABLE_ROW;

// Span Element Types

const BPElementType BPAutoLink       = Bypass::AUTOLINK;
const BPElementType BPCodeSpan       = Bypass::CODE_SPAN;
const BPElementType BPDoubleEmphasis = Bypass::DOUBLE_EMPHASIS;
const BPElementType BPEmphasis       = Bypass::EMPHASIS;
const BPElementType BPImage          = Bypass::IMAGE;
const BPElementType BPLineBreak      = Bypass::LINEBREAK;
const BPElementType BPLink           = Bypass::LINK;
const BPElementType BPRawHTMLTag     = Bypass::RAW_HTML_TAG;
const BPElementType BPTripleEmphasis = Bypass::TRIPLE_EMPHASIS;
const BPElementType BPText           = Bypass::TEXT;

@interface BPElement ()

@end

@implementation BPElement
{
    Bypass::Element _element;
    NSString        *_text;
    NSDictionary    *_attributes;
    NSArray         *_childElements;
}

- (id)init
{
    Bypass::Element element;
    return [self initWithElement:element];
}

- (id)initWithElement:(Bypass::Element)element
{
    self = [super init];
    
    if (self != nil) {
        _element = element;
    }
    
    return self;
}

- (BPElementType)elementType
{
    return _element.getType();
}

- (NSString *)text
{
    using namespace std;
    
    if (_text == nil) {
        string t = _element.getText();
        
        if (t.length() > 0) {
            _text = [NSString stringWithCString:t.c_str() encoding:NSUTF8StringEncoding];
        }
    }
    
    return _text;
}

- (NSDictionary *)attributes
{
    using namespace std;
    
    if (_attributes == nil) {
		Bypass::AttributeMap::iterator it = _element.attrBegin();
        NSMutableDictionary *attributes = [NSMutableDictionary dictionaryWithCapacity:_element.attrSize()];

		for (; it != _element.attrEnd(); ++it) {
			if (!it->first.empty() && !it->second.empty()) {
				NSString *nn = [NSString stringWithUTF8String:it->first.c_str()];
				NSString *vv = [NSString stringWithUTF8String:it->second.c_str()];

				[attributes setObject:vv forKey:nn];
			}
		}

        _attributes = [NSDictionary dictionaryWithDictionary:attributes];
    }
}

- (NSArray *)childElements
{
    using namespace Bypass;
    
    if (_childElements == nil) {
        size_t i, count = _element.size();
        
        NSMutableArray *childElements = [NSMutableArray arrayWithCapacity:count];
        
        for (i = 0; i < count; ++i) {
            Element c = _element[i];
            BPElement *cc = [[BPElement alloc] initWithElement:c];
            childElements[i] = cc;
        }
        
        _childElements = [NSArray arrayWithArray:childElements];
    }
    
    return _childElements;
}

@end