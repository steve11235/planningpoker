webpackJsonp(["styles"],{

/***/ "../../../../../src/background.png":
/***/ (function(module, exports) {

module.exports = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAABmJLR0QAMAAwAEAK1af5AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4QwSESo35B6w8gAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBkLmUHAAADP0lEQVR42u3dUVIqQQxAUaVYEEtw/4sCNzAlLZ1O0jPnfD+FZ9UlxoaZ78fj5/UFHLr5EYBAQCAgEBAICATau/9Zz61PP8/nM/T5RX+/mcfFBAGBgEBAIMDbJb1qoRxdlkefX8YyX7X0Y4KAQEAgIBC4yJLe/WTZEowJAgIBgYBA4DJL+sxJddUpvLeTY4KAQEAgIBA45ZIefVKdcfLtdB0TBAQCAgGBwCmX9CMZF3DL+NqMBd+pvgkCAgGBAAKBpUt6eKFFJ99VC77F3QQBgYBAQCDA0iV9dPHsfrX46P+vt96bICAQEAggEFi6pGcsvN2XW8u3CQIIBAQCAoH0JT366u5Vn13PeAz3STdBQCCAQEAgsHRJ77RkWngxQUAgIBAQCFx6ST8yc2Lc/bPrYIKAQEAgIBDYakmfqrHRafiOb8fHBAGBgEBAIGBJX7qgznxt1WfhvfXeBAEEAgIBgUCLJT3jfuA7LsEWdxMEBAIIBAQC6Ut61cly1Wl9xvfDBAGBgEBAIMBHS/pwecGLe9VJdcZ94TFBQCAgEBAI8HZJr7oau0UWEwQEAgIBgYBAAIGAQEAgIBAQCAgEBAICAYHA1d39CDZ6NbvYfeY7XJDQBAGBgEBAIGBJ56OFsvqPA6PPL2OZj1r6TRAQCAgEBAKWdMKX24w/BJzxXvEmCAgEBAICAUs6Uwv06Mlyp+dsgoBAQCAgEMCSvter2Yb3nt/9dN0EAYGAQEAgYElnWMYF3DK+NmPB/+T5mSAgEBAICAQs6dS9YhadfFct+K7uDn7FAoGAQMCSTouFt/vV4qP/v67uDn7FAoGAQMCSzvYLb/fPla98fiYICAQEAgIBSzrDC/To0trps+sZj+E+6eBXLBAICAQs6ax9NWt0on3Ge6KbICAQEAgIBCzpTJk5Me7+2XUTBAQCAgGBgCWd67w6NjoN7/p2fBMEBAICAYGAJZ3whTfja6s+C+/CceBXLBAICAQs6fR4hQt+a/vMY+z4czFBQCAgEBAIWNIJU3XLtKrT+ozvZ4KAQEAgIBCwpLP2lTB4ca86Xc+4L7wJAn7FAoGAQMCSzvRC+d9/F/24JggIBBAICAQEAgIBgUAzv2ohC1ZMWN0bAAAAAElFTkSuQmCC"

/***/ }),

/***/ "../../../../../src/styles.css":
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__("../../../../css-loader/index.js?{\"sourceMap\":false,\"importLoaders\":1}!../../../../postcss-loader/lib/index.js?{\"ident\":\"postcss\",\"sourceMap\":false}!../../../../../src/styles.css");
if(typeof content === 'string') content = [[module.i, content, '']];
// add the styles to the DOM
var update = __webpack_require__("../../../../style-loader/addStyles.js")(content, {});
if(content.locals) module.exports = content.locals;
// Hot Module Replacement
if(false) {
	// When the styles change, update the <style> tags
	if(!content.locals) {
		module.hot.accept("!!../node_modules/css-loader/index.js??ref--7-1!../node_modules/postcss-loader/lib/index.js??postcss!./styles.css", function() {
			var newContent = require("!!../node_modules/css-loader/index.js??ref--7-1!../node_modules/postcss-loader/lib/index.js??postcss!./styles.css");
			if(typeof newContent === 'string') newContent = [[module.id, newContent, '']];
			update(newContent);
		});
	}
	// When the module is disposed, remove the <style> tags
	module.hot.dispose(function() { update(); });
}

/***/ }),

/***/ "../../../../css-loader/index.js?{\"sourceMap\":false,\"importLoaders\":1}!../../../../postcss-loader/lib/index.js?{\"ident\":\"postcss\",\"sourceMap\":false}!../../../../../src/styles.css":
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("../../../../css-loader/lib/css-base.js")(false);
// imports


// module
exports.push([module.i, ":root {\n  --base-white: #EEF;\n  --base-font-size: 2vh;\n  --base-font-family: Arial, Liberation Sans, Sans Serif;\n  --base-background-color: #303040;\n  --background-highlight1-color: #585874;\n  --background-highlight2-color: #636383;\n}\n\nbody {\n  background-image: url(" + __webpack_require__("../../../../../src/background.png") + ");\n  font-size: 2vh;\n  font-size: var(--base-font-size);\n  font-family: Arial, Liberation Sans, Sans Serif;\n  font-family: var(--base-font-family);\n  color: #EEF;\n  color: var(--base-white);\n}\n\nh1 {\n  margin: .5em;\n  font-size: 3em;\n  font-style: italic;\n}\n\ninput {\n  border-color: #EEF;\n  border-color: var(--base-white);\n  border-radius: .3em;\n  padding: .2em;\n  font-size: 2vh;\n  font-size: var(--base-font-size);\n  font-family: Arial, Liberation Sans, Sans Serif;\n  font-family: var(--base-font-family);\n  color: #EEF;\n  color: var(--base-white);\n  background-color: #303040;\n  background-color: var(--base-background-color);\n}\n\ninput:disabled {\n  border-style: solid;\n  border-color: gray;\n}\n\nbutton {\n  margin: 0 .5em;\n  border-color: #EEF;\n  border-color: var(--base-white);\n  border-width: 2px;\n  border-radius: .3em;\n  padding: .2em;\n  font-size: 2vh;\n  font-size: var(--base-font-size);\n  font-family: Arial, Liberation Sans, Sans Serif;\n  font-family: var(--base-font-family);\n  color: #EEF;\n  color: var(--base-white);\n  background-color: #303040;\n  background-color: var(--base-background-color);\n}\n\nbutton:focus {\n  background-color: #585874;\n  background-color: var(--background-highlight1-color);\n}\n\nbutton:not([disabled]):hover {\n  background-color: #636383;\n  background-color: var(--background-highlight2-color);\n}\n\nbutton:disabled {\n  border-style: solid;\n  border-color: gray;\n}\n\ndiv.floatWrapper::after {\n  /* Add zero-height content after a div to clear float behavior */\n  content: \" \";\n  height: 0;\n  clear: both;\n}\n\n.cell {\n  /* float forces block; box-sizing allows width and height to include border and padding which forces button to behave consistently */\n  float: left;\n  margin: 0;\n  box-sizing: border-box;\n  width: 2.5em;\n  height: 2em;\n  border-width: 2px;\n  border-color: #EEF;\n  border-color: var(--base-white);\n  padding: .2em;\n}\n\nspan.cell {\n  border-style: solid;\n  border-color: #EEF;\n  border-color: var(--base-white);\n  border-radius: .3em;\n  text-align: right;\n}\n\n.w10 {\n  width: 10em;\n}\n\n.w15 {\n  width: 15em;\n}\n", ""]);

// exports


/***/ }),

/***/ "../../../../css-loader/lib/css-base.js":
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
module.exports = function(useSourceMap) {
	var list = [];

	// return the list of modules as css string
	list.toString = function toString() {
		return this.map(function (item) {
			var content = cssWithMappingToString(item, useSourceMap);
			if(item[2]) {
				return "@media " + item[2] + "{" + content + "}";
			} else {
				return content;
			}
		}).join("");
	};

	// import a list of modules into the list
	list.i = function(modules, mediaQuery) {
		if(typeof modules === "string")
			modules = [[null, modules, ""]];
		var alreadyImportedModules = {};
		for(var i = 0; i < this.length; i++) {
			var id = this[i][0];
			if(typeof id === "number")
				alreadyImportedModules[id] = true;
		}
		for(i = 0; i < modules.length; i++) {
			var item = modules[i];
			// skip already imported module
			// this implementation is not 100% perfect for weird media query combinations
			//  when a module is imported multiple times with different media queries.
			//  I hope this will never occur (Hey this way we have smaller bundles)
			if(typeof item[0] !== "number" || !alreadyImportedModules[item[0]]) {
				if(mediaQuery && !item[2]) {
					item[2] = mediaQuery;
				} else if(mediaQuery) {
					item[2] = "(" + item[2] + ") and (" + mediaQuery + ")";
				}
				list.push(item);
			}
		}
	};
	return list;
};

function cssWithMappingToString(item, useSourceMap) {
	var content = item[1] || '';
	var cssMapping = item[3];
	if (!cssMapping) {
		return content;
	}

	if (useSourceMap && typeof btoa === 'function') {
		var sourceMapping = toComment(cssMapping);
		var sourceURLs = cssMapping.sources.map(function (source) {
			return '/*# sourceURL=' + cssMapping.sourceRoot + source + ' */'
		});

		return [content].concat(sourceURLs).concat([sourceMapping]).join('\n');
	}

	return [content].join('\n');
}

// Adapted from convert-source-map (MIT)
function toComment(sourceMap) {
	// eslint-disable-next-line no-undef
	var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap))));
	var data = 'sourceMappingURL=data:application/json;charset=utf-8;base64,' + base64;

	return '/*# ' + data + ' */';
}


/***/ }),

/***/ "../../../../style-loader/addStyles.js":
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
var stylesInDom = {},
	memoize = function(fn) {
		var memo;
		return function () {
			if (typeof memo === "undefined") memo = fn.apply(this, arguments);
			return memo;
		};
	},
	isOldIE = memoize(function() {
		return /msie [6-9]\b/.test(self.navigator.userAgent.toLowerCase());
	}),
	getHeadElement = memoize(function () {
		return document.head || document.getElementsByTagName("head")[0];
	}),
	singletonElement = null,
	singletonCounter = 0,
	styleElementsInsertedAtTop = [];

module.exports = function(list, options) {
	if(typeof DEBUG !== "undefined" && DEBUG) {
		if(typeof document !== "object") throw new Error("The style-loader cannot be used in a non-browser environment");
	}

	options = options || {};
	// Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
	// tags it will allow on a page
	if (typeof options.singleton === "undefined") options.singleton = isOldIE();

	// By default, add <style> tags to the bottom of <head>.
	if (typeof options.insertAt === "undefined") options.insertAt = "bottom";

	var styles = listToStyles(list);
	addStylesToDom(styles, options);

	return function update(newList) {
		var mayRemove = [];
		for(var i = 0; i < styles.length; i++) {
			var item = styles[i];
			var domStyle = stylesInDom[item.id];
			domStyle.refs--;
			mayRemove.push(domStyle);
		}
		if(newList) {
			var newStyles = listToStyles(newList);
			addStylesToDom(newStyles, options);
		}
		for(var i = 0; i < mayRemove.length; i++) {
			var domStyle = mayRemove[i];
			if(domStyle.refs === 0) {
				for(var j = 0; j < domStyle.parts.length; j++)
					domStyle.parts[j]();
				delete stylesInDom[domStyle.id];
			}
		}
	};
}

function addStylesToDom(styles, options) {
	for(var i = 0; i < styles.length; i++) {
		var item = styles[i];
		var domStyle = stylesInDom[item.id];
		if(domStyle) {
			domStyle.refs++;
			for(var j = 0; j < domStyle.parts.length; j++) {
				domStyle.parts[j](item.parts[j]);
			}
			for(; j < item.parts.length; j++) {
				domStyle.parts.push(addStyle(item.parts[j], options));
			}
		} else {
			var parts = [];
			for(var j = 0; j < item.parts.length; j++) {
				parts.push(addStyle(item.parts[j], options));
			}
			stylesInDom[item.id] = {id: item.id, refs: 1, parts: parts};
		}
	}
}

function listToStyles(list) {
	var styles = [];
	var newStyles = {};
	for(var i = 0; i < list.length; i++) {
		var item = list[i];
		var id = item[0];
		var css = item[1];
		var media = item[2];
		var sourceMap = item[3];
		var part = {css: css, media: media, sourceMap: sourceMap};
		if(!newStyles[id])
			styles.push(newStyles[id] = {id: id, parts: [part]});
		else
			newStyles[id].parts.push(part);
	}
	return styles;
}

function insertStyleElement(options, styleElement) {
	var head = getHeadElement();
	var lastStyleElementInsertedAtTop = styleElementsInsertedAtTop[styleElementsInsertedAtTop.length - 1];
	if (options.insertAt === "top") {
		if(!lastStyleElementInsertedAtTop) {
			head.insertBefore(styleElement, head.firstChild);
		} else if(lastStyleElementInsertedAtTop.nextSibling) {
			head.insertBefore(styleElement, lastStyleElementInsertedAtTop.nextSibling);
		} else {
			head.appendChild(styleElement);
		}
		styleElementsInsertedAtTop.push(styleElement);
	} else if (options.insertAt === "bottom") {
		head.appendChild(styleElement);
	} else {
		throw new Error("Invalid value for parameter 'insertAt'. Must be 'top' or 'bottom'.");
	}
}

function removeStyleElement(styleElement) {
	styleElement.parentNode.removeChild(styleElement);
	var idx = styleElementsInsertedAtTop.indexOf(styleElement);
	if(idx >= 0) {
		styleElementsInsertedAtTop.splice(idx, 1);
	}
}

function createStyleElement(options) {
	var styleElement = document.createElement("style");
	styleElement.type = "text/css";
	insertStyleElement(options, styleElement);
	return styleElement;
}

function createLinkElement(options) {
	var linkElement = document.createElement("link");
	linkElement.rel = "stylesheet";
	insertStyleElement(options, linkElement);
	return linkElement;
}

function addStyle(obj, options) {
	var styleElement, update, remove;

	if (options.singleton) {
		var styleIndex = singletonCounter++;
		styleElement = singletonElement || (singletonElement = createStyleElement(options));
		update = applyToSingletonTag.bind(null, styleElement, styleIndex, false);
		remove = applyToSingletonTag.bind(null, styleElement, styleIndex, true);
	} else if(obj.sourceMap &&
		typeof URL === "function" &&
		typeof URL.createObjectURL === "function" &&
		typeof URL.revokeObjectURL === "function" &&
		typeof Blob === "function" &&
		typeof btoa === "function") {
		styleElement = createLinkElement(options);
		update = updateLink.bind(null, styleElement);
		remove = function() {
			removeStyleElement(styleElement);
			if(styleElement.href)
				URL.revokeObjectURL(styleElement.href);
		};
	} else {
		styleElement = createStyleElement(options);
		update = applyToTag.bind(null, styleElement);
		remove = function() {
			removeStyleElement(styleElement);
		};
	}

	update(obj);

	return function updateStyle(newObj) {
		if(newObj) {
			if(newObj.css === obj.css && newObj.media === obj.media && newObj.sourceMap === obj.sourceMap)
				return;
			update(obj = newObj);
		} else {
			remove();
		}
	};
}

var replaceText = (function () {
	var textStore = [];

	return function (index, replacement) {
		textStore[index] = replacement;
		return textStore.filter(Boolean).join('\n');
	};
})();

function applyToSingletonTag(styleElement, index, remove, obj) {
	var css = remove ? "" : obj.css;

	if (styleElement.styleSheet) {
		styleElement.styleSheet.cssText = replaceText(index, css);
	} else {
		var cssNode = document.createTextNode(css);
		var childNodes = styleElement.childNodes;
		if (childNodes[index]) styleElement.removeChild(childNodes[index]);
		if (childNodes.length) {
			styleElement.insertBefore(cssNode, childNodes[index]);
		} else {
			styleElement.appendChild(cssNode);
		}
	}
}

function applyToTag(styleElement, obj) {
	var css = obj.css;
	var media = obj.media;

	if(media) {
		styleElement.setAttribute("media", media)
	}

	if(styleElement.styleSheet) {
		styleElement.styleSheet.cssText = css;
	} else {
		while(styleElement.firstChild) {
			styleElement.removeChild(styleElement.firstChild);
		}
		styleElement.appendChild(document.createTextNode(css));
	}
}

function updateLink(linkElement, obj) {
	var css = obj.css;
	var sourceMap = obj.sourceMap;

	if(sourceMap) {
		// http://stackoverflow.com/a/26603875
		css += "\n/*# sourceMappingURL=data:application/json;base64," + btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))) + " */";
	}

	var blob = new Blob([css], { type: "text/css" });

	var oldSrc = linkElement.href;

	linkElement.href = URL.createObjectURL(blob);

	if(oldSrc)
		URL.revokeObjectURL(oldSrc);
}


/***/ }),

/***/ 2:
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__("../../../../../src/styles.css");


/***/ })

},[2]);
//# sourceMappingURL=styles.bundle.js.map