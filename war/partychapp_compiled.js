var i, aa = aa || {}, m = this;
function o(a, b, c) {
  a = a.split(".");
  c = c || m;
  !(a[0] in c) && c.execScript && c.execScript("var " + a[0]);
  for(var d;a.length && (d = a.shift());) {
    if(!a.length && b !== undefined) {
      c[d] = b
    }else {
      c = c[d] ? c[d] : (c[d] = {})
    }
  }
}
function ba(a, b) {
  for(var c = a.split("."), d = b || m, f;f = c.shift();) {
    if(d[f]) {
      d = d[f]
    }else {
      return null
    }
  }
  return d
}
function p() {
}
function q(a) {
  var b = typeof a;
  if(b == "object") {
    if(a) {
      if(a instanceof Array || !(a instanceof Object) && Object.prototype.toString.call(a) == "[object Array]" || typeof a.length == "number" && typeof a.splice != "undefined" && typeof a.propertyIsEnumerable != "undefined" && !a.propertyIsEnumerable("splice")) {
        return"array"
      }
      if(!(a instanceof Object) && (Object.prototype.toString.call(a) == "[object Function]" || typeof a.call != "undefined" && typeof a.propertyIsEnumerable != "undefined" && !a.propertyIsEnumerable("call"))) {
        return"function"
      }
    }else {
      return"null"
    }
  }else {
    if(b == "function" && typeof a.call == "undefined") {
      return"object"
    }
  }
  return b
}
function r(a) {
  var b = q(a);
  return b == "array" || b == "object" && typeof a.length == "number"
}
function s(a) {
  return typeof a == "string"
}
function ca(a) {
  return q(a) == "function"
}
function da(a) {
  a = q(a);
  return a == "object" || a == "array" || a == "function"
}
function t(a) {
  if(a.hasOwnProperty && a.hasOwnProperty(u)) {
    return a[u]
  }
  a[u] || (a[u] = ++ea);
  return a[u]
}
var u = "closure_uid_" + Math.floor(Math.random() * 2147483648).toString(36), ea = 0;
function v(a, b) {
  var c = b || m;
  if(arguments.length > 2) {
    var d = Array.prototype.slice.call(arguments, 2);
    return function() {
      var f = Array.prototype.slice.call(arguments);
      Array.prototype.unshift.apply(f, d);
      return a.apply(c, f)
    }
  }else {
    return function() {
      return a.apply(c, arguments)
    }
  }
}
function fa(a) {
  var b = Array.prototype.slice.call(arguments, 1);
  return function() {
    var c = Array.prototype.slice.call(arguments);
    c.unshift.apply(c, b);
    return a.apply(this, c)
  }
}
var ga = Date.now || function() {
  return+new Date
};
function y(a, b) {
  function c() {
  }
  c.prototype = b.prototype;
  a.p = b.prototype;
  a.prototype = new c
}
;function ha(a) {
  this.stack = (new Error).stack || "";
  if(a) {
    this.message = String(a)
  }
}
y(ha, Error);
ha.prototype.name = "CustomError";function ia(a) {
  for(var b = 1;b < arguments.length;b++) {
    var c = String(arguments[b]).replace(/\$/g, "$$$$");
    a = a.replace(/\%s/, c)
  }
  return a
}
function z(a, b) {
  if(b) {
    return a.replace(ja, "&amp;").replace(ka, "&lt;").replace(la, "&gt;").replace(ma, "&quot;")
  }else {
    if(!na.test(a)) {
      return a
    }
    if(a.indexOf("&") != -1) {
      a = a.replace(ja, "&amp;")
    }
    if(a.indexOf("<") != -1) {
      a = a.replace(ka, "&lt;")
    }
    if(a.indexOf(">") != -1) {
      a = a.replace(la, "&gt;")
    }
    if(a.indexOf('"') != -1) {
      a = a.replace(ma, "&quot;")
    }
    return a
  }
}
var ja = /&/g, ka = /</g, la = />/g, ma = /\"/g, na = /[&<>\"]/;
function oa(a, b) {
  for(var c = 0, d = String(a).replace(/^[\s\xa0]+|[\s\xa0]+$/g, "").split("."), f = String(b).replace(/^[\s\xa0]+|[\s\xa0]+$/g, "").split("."), e = Math.max(d.length, f.length), g = 0;c == 0 && g < e;g++) {
    var h = d[g] || "", j = f[g] || "", k = new RegExp("(\\d*)(\\D*)", "g"), x = new RegExp("(\\d*)(\\D*)", "g");
    do {
      var n = k.exec(h) || ["", "", ""], l = x.exec(j) || ["", "", ""];
      if(n[0].length == 0 && l[0].length == 0) {
        break
      }
      c = pa(n[1].length == 0 ? 0 : parseInt(n[1], 10), l[1].length == 0 ? 0 : parseInt(l[1], 10)) || pa(n[2].length == 0, l[2].length == 0) || pa(n[2], l[2])
    }while(c == 0)
  }
  return c
}
function pa(a, b) {
  if(a < b) {
    return-1
  }else {
    if(a > b) {
      return 1
    }
  }
  return 0
}
;function qa(a, b) {
  b.unshift(a);
  ha.call(this, ia.apply(null, b));
  b.shift();
  this.Ya = a
}
y(qa, ha);
qa.prototype.name = "AssertionError";
function A(a, b) {
  if(!a) {
    var c = Array.prototype.slice.call(arguments, 2), d = "Assertion failed";
    if(b) {
      d += ": " + b;
      var f = c
    }
    throw new qa("" + d, f || []);
  }
}
function ra(a) {
  throw new qa("Failure" + (a ? ": " + a : ""), Array.prototype.slice.call(arguments, 1));
}
;var B = Array.prototype, C = B.indexOf ? function(a, b, c) {
  A(a.length != null);
  return B.indexOf.call(a, b, c)
} : function(a, b, c) {
  c = c == null ? 0 : c < 0 ? Math.max(0, a.length + c) : c;
  if(s(a)) {
    if(!s(b) || b.length != 1) {
      return-1
    }
    return a.indexOf(b, c)
  }
  for(c = c;c < a.length;c++) {
    if(c in a && a[c] === b) {
      return c
    }
  }
  return-1
}, D = B.forEach ? function(a, b, c) {
  A(a.length != null);
  B.forEach.call(a, b, c)
} : function(a, b, c) {
  for(var d = a.length, f = s(a) ? a.split("") : a, e = 0;e < d;e++) {
    e in f && b.call(c, f[e], e, a)
  }
};
function sa(a, b) {
  var c = C(a, b), d;
  if(d = c >= 0) {
    A(a.length != null);
    B.splice.call(a, c, 1)
  }
  return d
}
function ta() {
  return B.concat.apply(B, arguments)
}
function ua(a) {
  if(q(a) == "array") {
    return ta(a)
  }else {
    for(var b = [], c = 0, d = a.length;c < d;c++) {
      b[c] = a[c]
    }
    return b
  }
}
function va(a) {
  A(a.length != null);
  return B.splice.apply(a, wa(arguments, 1))
}
function wa(a, b, c) {
  A(a.length != null);
  return arguments.length <= 2 ? B.slice.call(a, b) : B.slice.call(a, b, c)
}
;function xa(a) {
  return(a = a.className) && typeof a.split == "function" ? a.split(/\s+/) : []
}
function E(a) {
  var b = xa(a), c;
  c = wa(arguments, 1);
  for(var d = 0, f = 0;f < c.length;f++) {
    if(!(C(b, c[f]) >= 0)) {
      b.push(c[f]);
      d++
    }
  }
  c = d == c.length;
  a.className = b.join(" ");
  return c
}
function F(a) {
  var b = xa(a), c;
  c = wa(arguments, 1);
  for(var d = 0, f = 0;f < b.length;f++) {
    if(C(c, b[f]) >= 0) {
      va(b, f--, 1);
      d++
    }
  }
  c = d == c.length;
  a.className = b.join(" ");
  return c
}
function ya(a, b) {
  var c = !(C(xa(a), b) >= 0);
  c ? E(a, b) : F(a, b);
  return c
}
;function za(a, b, c) {
  for(var d in a) {
    b.call(c, a[d], d, a)
  }
}
function Aa(a) {
  var b = [], c = 0;
  for(var d in a) {
    b[c++] = a[d]
  }
  return b
}
function Ba(a) {
  var b = [], c = 0;
  for(var d in a) {
    b[c++] = d
  }
  return b
}
var Ca = ["constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable", "toLocaleString", "toString", "valueOf"];
function Da(a) {
  for(var b, c, d = 1;d < arguments.length;d++) {
    c = arguments[d];
    for(b in c) {
      a[b] = c[b]
    }
    for(var f = 0;f < Ca.length;f++) {
      b = Ca[f];
      if(Object.prototype.hasOwnProperty.call(c, b)) {
        a[b] = c[b]
      }
    }
  }
}
;var G, Ea, Fa, Ga;
function Ha() {
  return m.navigator ? m.navigator.userAgent : null
}
Ga = Fa = Ea = G = false;
var Ia;
if(Ia = Ha()) {
  var Ja = m.navigator;
  G = Ia.indexOf("Opera") == 0;
  Ea = !G && Ia.indexOf("MSIE") != -1;
  Fa = !G && Ia.indexOf("WebKit") != -1;
  Ga = !G && !Fa && Ja.product == "Gecko"
}
var Ka = G, La = Ea, Ma = Ga, Na = Fa, Oa = m.navigator, Pa = (Oa && Oa.platform || "").indexOf("Mac") != -1, Qa, Ra = "", H;
if(Ka && m.opera) {
  var Sa = m.opera.version;
  Ra = typeof Sa == "function" ? Sa() : Sa
}else {
  if(Ma) {
    H = /rv\:([^\);]+)(\)|;)/
  }else {
    if(La) {
      H = /MSIE\s+([^\);]+)(\)|;)/
    }else {
      if(Na) {
        H = /WebKit\/(\S+)/
      }
    }
  }
  if(H) {
    var Ta = H.exec(Ha());
    Ra = Ta ? Ta[1] : ""
  }
}
Qa = Ra;
var Ua = {};function I(a) {
  return s(a) ? document.getElementById(a) : a
}
function Va(a, b, c, d) {
  a = d || a;
  b = b && b != "*" ? b.toUpperCase() : "";
  if(a.querySelectorAll && a.querySelector && (!Na || document.compatMode == "CSS1Compat" || Ua["528"] || (Ua["528"] = oa(Qa, "528") >= 0)) && (b || c)) {
    return a.querySelectorAll(b + (c ? "." + c : ""))
  }
  if(c && a.getElementsByClassName) {
    a = a.getElementsByClassName(c);
    if(b) {
      d = {};
      for(var f = 0, e = 0, g;g = a[e];e++) {
        if(b == g.nodeName) {
          d[f++] = g
        }
      }
      d.length = f;
      return d
    }else {
      return a
    }
  }
  a = a.getElementsByTagName(b || "*");
  if(c) {
    d = {};
    for(e = f = 0;g = a[e];e++) {
      b = g.className;
      if(typeof b.split == "function" && C(b.split(/\s+/), c) >= 0) {
        d[f++] = g
      }
    }
    d.length = f;
    return d
  }else {
    return a
  }
}
function Wa(a, b) {
  za(b, function(c, d) {
    if(d == "style") {
      a.style.cssText = c
    }else {
      if(d == "class") {
        a.className = c
      }else {
        if(d == "for") {
          a.htmlFor = c
        }else {
          if(d in Xa) {
            a.setAttribute(Xa[d], c)
          }else {
            a[d] = c
          }
        }
      }
    }
  })
}
var Xa = {cellpadding:"cellPadding", cellspacing:"cellSpacing", colspan:"colSpan", rowspan:"rowSpan", valign:"vAlign", height:"height", width:"width", usemap:"useMap", frameborder:"frameBorder", type:"type"};
function Ya(a, b, c, d) {
  function f(g) {
    if(g) {
      b.appendChild(s(g) ? a.createTextNode(g) : g)
    }
  }
  for(d = d;d < c.length;d++) {
    var e = c[d];
    r(e) && !(da(e) && e.nodeType > 0) ? D(Za(e) ? ua(e) : e, f) : f(e)
  }
}
function J() {
  var a = arguments, b = a[0], c = a[1];
  if(La && c && (c.name || c.type)) {
    b = ["<", b];
    c.name && b.push(' name="', z(c.name), '"');
    if(c.type) {
      b.push(' type="', z(c.type), '"');
      var d = {};
      Da(d, c);
      c = d;
      delete c.type
    }
    b.push(">");
    b = b.join("")
  }
  b = document.createElement(b);
  if(c) {
    if(s(c)) {
      b.className = c
    }else {
      q(c) == "array" ? E.apply(null, [b].concat(c)) : Wa(b, c)
    }
  }
  a.length > 2 && Ya(document, b, a, 2);
  return b
}
function Za(a) {
  if(a && typeof a.length == "number") {
    if(da(a)) {
      return typeof a.item == "function" || typeof a.item == "string"
    }else {
      if(ca(a)) {
        return typeof a.item == "function"
      }
    }
  }
  return false
}
;function K() {
}
K.prototype.ma = false;
K.prototype.u = function() {
  if(!this.ma) {
    this.ma = true;
    this.f()
  }
};
K.prototype.f = function() {
};var $a = [];var ab;
function L(a, b) {
  this.type = a;
  this.currentTarget = this.target = b
}
y(L, K);
L.prototype.f = function() {
  delete this.type;
  delete this.target;
  delete this.currentTarget
};
L.prototype.n = false;
L.prototype.P = true;function M(a, b) {
  a && this.M(a, b)
}
y(M, L);
i = M.prototype;
i.target = null;
i.relatedTarget = null;
i.offsetX = 0;
i.offsetY = 0;
i.clientX = 0;
i.clientY = 0;
i.screenX = 0;
i.screenY = 0;
i.button = 0;
i.keyCode = 0;
i.charCode = 0;
i.ctrlKey = false;
i.altKey = false;
i.shiftKey = false;
i.metaKey = false;
i.Pa = false;
i.oa = null;
i.M = function(a, b) {
  var c = this.type = a.type;
  this.target = a.target || a.srcElement;
  this.currentTarget = b;
  var d = a.relatedTarget;
  if(d) {
    if(Ma) {
      try {
        d = d.nodeName && d
      }catch(f) {
        d = null
      }
    }
  }else {
    if(c == "mouseover") {
      d = a.fromElement
    }else {
      if(c == "mouseout") {
        d = a.toElement
      }
    }
  }
  this.relatedTarget = d;
  this.offsetX = a.offsetX !== undefined ? a.offsetX : a.layerX;
  this.offsetY = a.offsetY !== undefined ? a.offsetY : a.layerY;
  this.clientX = a.clientX !== undefined ? a.clientX : a.pageX;
  this.clientY = a.clientY !== undefined ? a.clientY : a.pageY;
  this.screenX = a.screenX || 0;
  this.screenY = a.screenY || 0;
  this.button = a.button;
  this.keyCode = a.keyCode || 0;
  this.charCode = a.charCode || (c == "keypress" ? a.keyCode : 0);
  this.ctrlKey = a.ctrlKey;
  this.altKey = a.altKey;
  this.shiftKey = a.shiftKey;
  this.metaKey = a.metaKey;
  this.Pa = Pa ? a.metaKey : a.ctrlKey;
  this.oa = a;
  delete this.P;
  delete this.n
};
La && (Ua["8"] || (Ua["8"] = oa(Qa, "8") >= 0));
M.prototype.f = function() {
  M.p.f.call(this);
  this.relatedTarget = this.currentTarget = this.target = this.oa = null
};function bb() {
}
var cb = 0;
i = bb.prototype;
i.key = 0;
i.o = false;
i.ia = false;
i.M = function(a, b, c, d, f, e) {
  if(ca(a)) {
    this.sa = true
  }else {
    if(a && a.handleEvent && ca(a.handleEvent)) {
      this.sa = false
    }else {
      throw Error("Invalid listener argument");
    }
  }
  this.C = a;
  this.za = b;
  this.src = c;
  this.type = d;
  this.capture = !!f;
  this.aa = e;
  this.ia = false;
  this.key = ++cb;
  this.o = false
};
i.handleEvent = function(a) {
  if(this.sa) {
    return this.C.call(this.aa || this.src, a)
  }
  return this.C.handleEvent.call(this.C, a)
};function N(a, b) {
  this.va = b;
  this.k = [];
  if(a > this.va) {
    throw Error("[goog.structs.SimplePool] Initial cannot be greater than max");
  }
  for(var c = 0;c < a;c++) {
    this.k.push(this.t())
  }
}
y(N, K);
N.prototype.j = null;
N.prototype.la = null;
function O(a) {
  if(a.k.length) {
    return a.k.pop()
  }
  return a.t()
}
function P(a, b) {
  a.k.length < a.va ? a.k.push(b) : a.X(b)
}
N.prototype.t = function() {
  return this.j ? this.j() : {}
};
N.prototype.X = function(a) {
  if(this.la) {
    this.la(a)
  }else {
    if(da(a)) {
      if(ca(a.u)) {
        a.u()
      }else {
        for(var b in a) {
          delete a[b]
        }
      }
    }
  }
};
N.prototype.f = function() {
  N.p.f.call(this);
  for(var a = this.k;a.length;) {
    this.X(a.pop())
  }
  delete this.k
};var db;
var eb = (db = "ScriptEngine" in m && m.ScriptEngine() == "JScript") ? m.ScriptEngineMajorVersion() + "." + m.ScriptEngineMinorVersion() + "." + m.ScriptEngineBuildVersion() : "0";var fb, gb, hb, ib, jb, kb, lb, mb, nb, ob, pb;
(function() {
  function a() {
    return{c:0, e:0}
  }
  function b() {
    return[]
  }
  function c() {
    function l(w) {
      return g.call(l.src, l.key, w)
    }
    return l
  }
  function d() {
    return new bb
  }
  function f() {
    return new M
  }
  var e = db && !(oa(eb, "5.7") >= 0), g;
  kb = function(l) {
    g = l
  };
  if(e) {
    fb = function() {
      return O(h)
    };
    gb = function(l) {
      P(h, l)
    };
    hb = function() {
      return O(j)
    };
    ib = function(l) {
      P(j, l)
    };
    jb = function() {
      return O(k)
    };
    lb = function() {
      P(k, c())
    };
    mb = function() {
      return O(x)
    };
    nb = function(l) {
      P(x, l)
    };
    ob = function() {
      return O(n)
    };
    pb = function(l) {
      P(n, l)
    };
    var h = new N(0, 600);
    h.j = a;
    var j = new N(0, 600);
    j.j = b;
    var k = new N(0, 600);
    k.j = c;
    var x = new N(0, 600);
    x.j = d;
    var n = new N(0, 600);
    n.j = f
  }else {
    fb = a;
    gb = p;
    hb = b;
    ib = p;
    jb = c;
    lb = p;
    mb = d;
    nb = p;
    ob = f;
    pb = p
  }
})();var Q = {}, R = {}, S = {}, qb = {};
function rb(a, b, c, d, f) {
  if(b) {
    if(q(b) == "array") {
      for(var e = 0;e < b.length;e++) {
        rb(a, b[e], c, d, f)
      }
      return null
    }else {
      d = !!d;
      var g = R;
      b in g || (g[b] = fb());
      g = g[b];
      if(!(d in g)) {
        g[d] = fb();
        g.c++
      }
      g = g[d];
      var h = t(a), j;
      g.e++;
      if(g[h]) {
        j = g[h];
        for(e = 0;e < j.length;e++) {
          g = j[e];
          if(g.C == c && g.aa == f) {
            if(g.o) {
              break
            }
            return j[e].key
          }
        }
      }else {
        j = g[h] = hb();
        g.c++
      }
      e = jb();
      e.src = a;
      g = mb();
      g.M(c, e, a, b, d, f);
      c = g.key;
      e.key = c;
      j.push(g);
      Q[c] = g;
      S[h] || (S[h] = hb());
      S[h].push(g);
      if(a.addEventListener) {
        if(a == m || !a.ka) {
          a.addEventListener(b, e, d)
        }
      }else {
        a.attachEvent(sb(b), e)
      }
      return c
    }
  }else {
    throw Error("Invalid event type");
  }
}
function tb(a, b, c, d, f) {
  if(q(b) == "array") {
    for(var e = 0;e < b.length;e++) {
      tb(a, b[e], c, d, f)
    }
    return null
  }
  d = !!d;
  a: {
    e = R;
    if(b in e) {
      e = e[b];
      if(d in e) {
        e = e[d];
        a = t(a);
        if(e[a]) {
          a = e[a];
          break a
        }
      }
    }
    a = null
  }
  if(!a) {
    return false
  }
  for(e = 0;e < a.length;e++) {
    if(a[e].C == c && a[e].capture == d && a[e].aa == f) {
      return ub(a[e].key)
    }
  }
  return false
}
function ub(a) {
  if(!Q[a]) {
    return false
  }
  var b = Q[a];
  if(b.o) {
    return false
  }
  var c = b.src, d = b.type, f = b.za, e = b.capture;
  if(c.removeEventListener) {
    if(c == m || !c.ka) {
      c.removeEventListener(d, f, e)
    }
  }else {
    c.detachEvent && c.detachEvent(sb(d), f)
  }
  c = t(c);
  f = R[d][e][c];
  if(S[c]) {
    var g = S[c];
    sa(g, b);
    g.length == 0 && delete S[c]
  }
  b.o = true;
  f.xa = true;
  vb(d, e, c, f);
  delete Q[a];
  return true
}
function vb(a, b, c, d) {
  if(!d.N) {
    if(d.xa) {
      for(var f = 0, e = 0;f < d.length;f++) {
        if(d[f].o) {
          var g = d[f].za;
          g.src = null;
          lb(g);
          nb(d[f])
        }else {
          if(f != e) {
            d[e] = d[f]
          }
          e++
        }
      }
      d.length = e;
      d.xa = false;
      if(e == 0) {
        ib(d);
        delete R[a][b][c];
        R[a][b].c--;
        if(R[a][b].c == 0) {
          gb(R[a][b]);
          delete R[a][b];
          R[a].c--
        }
        if(R[a].c == 0) {
          gb(R[a]);
          delete R[a]
        }
      }
    }
  }
}
function wb(a, b, c) {
  var d = 0, f = b == null, e = c == null;
  c = !!c;
  if(a == null) {
    za(S, function(j) {
      for(var k = j.length - 1;k >= 0;k--) {
        var x = j[k];
        if((f || b == x.type) && (e || c == x.capture)) {
          ub(x.key);
          d++
        }
      }
    })
  }else {
    a = t(a);
    if(S[a]) {
      a = S[a];
      for(var g = a.length - 1;g >= 0;g--) {
        var h = a[g];
        if((f || b == h.type) && (e || c == h.capture)) {
          ub(h.key);
          d++
        }
      }
    }
  }
  return d
}
function sb(a) {
  if(a in qb) {
    return qb[a]
  }
  return qb[a] = "on" + a
}
function xb(a, b, c, d, f) {
  var e = 1;
  b = t(b);
  if(a[b]) {
    a.e--;
    a = a[b];
    if(a.N) {
      a.N++
    }else {
      a.N = 1
    }
    try {
      for(var g = a.length, h = 0;h < g;h++) {
        var j = a[h];
        if(j && !j.o) {
          e &= yb(j, f) !== false
        }
      }
    }finally {
      a.N--;
      vb(c, d, b, a)
    }
  }
  return Boolean(e)
}
function yb(a, b) {
  var c = a.handleEvent(b);
  a.ia && ub(a.key);
  return c
}
function zb(a, b) {
  if(!Q[a]) {
    return true
  }
  var c = Q[a], d = c.type, f = R;
  if(!(d in f)) {
    return true
  }
  f = f[d];
  var e, g;
  if(ab === undefined) {
    ab = La && !m.addEventListener
  }
  if(ab) {
    e = b || ba("window.event");
    var h = true in f, j = false in f;
    if(h) {
      if(e.keyCode < 0 || e.returnValue != undefined) {
        return true
      }
      a: {
        var k = false;
        if(e.keyCode == 0) {
          try {
            e.keyCode = -1;
            break a
          }catch(x) {
            k = true
          }
        }
        if(k || e.returnValue == undefined) {
          e.returnValue = true
        }
      }
    }
    k = ob();
    k.M(e, this);
    e = true;
    try {
      if(h) {
        for(var n = hb(), l = k.currentTarget;l;l = l.parentNode) {
          n.push(l)
        }
        g = f[true];
        g.e = g.c;
        for(var w = n.length - 1;!k.n && w >= 0 && g.e;w--) {
          k.currentTarget = n[w];
          e &= xb(g, n[w], d, true, k)
        }
        if(j) {
          g = f[false];
          g.e = g.c;
          for(w = 0;!k.n && w < n.length && g.e;w++) {
            k.currentTarget = n[w];
            e &= xb(g, n[w], d, false, k)
          }
        }
      }else {
        e = yb(c, k)
      }
    }finally {
      if(n) {
        n.length = 0;
        ib(n)
      }
      k.u();
      pb(k)
    }
    return e
  }
  d = new M(b, this);
  try {
    e = yb(c, d)
  }finally {
    d.u()
  }
  return e
}
kb(zb);
$a[$a.length] = function(a) {
  zb = a.Ua(zb);
  kb(zb)
};function Ab() {
}
y(Ab, K);
i = Ab.prototype;
i.ka = true;
i.ga = null;
i.addEventListener = function(a, b, c, d) {
  rb(this, a, b, c, d)
};
i.removeEventListener = function(a, b, c, d) {
  tb(this, a, b, c, d)
};
i.dispatchEvent = function(a) {
  a = a;
  if(s(a)) {
    a = new L(a, this)
  }else {
    if(a instanceof L) {
      a.target = a.target || this
    }else {
      var b = a;
      a = new L(a.type, this);
      Da(a, b)
    }
  }
  b = 1;
  var c, d = a.type, f = R;
  if(d in f) {
    f = f[d];
    d = true in f;
    var e;
    if(d) {
      c = [];
      for(e = this;e;e = e.ga) {
        c.push(e)
      }
      e = f[true];
      e.e = e.c;
      for(var g = c.length - 1;!a.n && g >= 0 && e.e;g--) {
        a.currentTarget = c[g];
        b &= xb(e, c[g], a.type, true, a) && a.P != false
      }
    }
    if(false in f) {
      e = f[false];
      e.e = e.c;
      if(d) {
        for(g = 0;!a.n && g < c.length && e.e;g++) {
          a.currentTarget = c[g];
          b &= xb(e, c[g], a.type, false, a) && a.P != false
        }
      }else {
        for(c = this;!a.n && c && e.e;c = c.ga) {
          a.currentTarget = c;
          b &= xb(e, c, a.type, false, a) && a.P != false
        }
      }
    }
    a = Boolean(b)
  }else {
    a = true
  }
  return a
};
i.f = function() {
  Ab.p.f.call(this);
  wb(this);
  this.ga = null
};var Bb = m.window;function Cb(a) {
  if(typeof a.K == "function") {
    return a.K()
  }
  if(s(a)) {
    return a.split("")
  }
  if(r(a)) {
    for(var b = [], c = a.length, d = 0;d < c;d++) {
      b.push(a[d])
    }
    return b
  }
  return Aa(a)
}
function Db(a, b, c) {
  if(typeof a.forEach == "function") {
    a.forEach(b, c)
  }else {
    if(r(a) || s(a)) {
      D(a, b, c)
    }else {
      var d;
      if(typeof a.w == "function") {
        d = a.w()
      }else {
        if(typeof a.K != "function") {
          if(r(a) || s(a)) {
            d = [];
            for(var f = a.length, e = 0;e < f;e++) {
              d.push(e)
            }
            d = d
          }else {
            d = Ba(a)
          }
        }else {
          d = void 0
        }
      }
      f = Cb(a);
      e = f.length;
      for(var g = 0;g < e;g++) {
        b.call(c, f[g], d && d[g], a)
      }
    }
  }
}
;var Eb = "StopIteration" in m ? m.StopIteration : Error("StopIteration");
function Fb() {
}
Fb.prototype.next = function() {
  throw Eb;
};
Fb.prototype.U = function() {
  return this
};
function Gb(a) {
  if(a instanceof Fb) {
    return a
  }
  if(typeof a.U == "function") {
    return a.U(false)
  }
  if(r(a)) {
    var b = 0, c = new Fb;
    c.next = function() {
      for(;;) {
        if(b >= a.length) {
          throw Eb;
        }
        if(b in a) {
          return a[b++]
        }else {
          b++
        }
      }
    };
    return c
  }
  throw Error("Not implemented");
}
function Hb(a, b, c) {
  if(r(a)) {
    try {
      D(a, b, c)
    }catch(d) {
      if(d !== Eb) {
        throw d;
      }
    }
  }else {
    a = Gb(a);
    try {
      for(;;) {
        b.call(c, a.next(), undefined, a)
      }
    }catch(f) {
      if(f !== Eb) {
        throw f;
      }
    }
  }
}
;function T(a) {
  this.g = {};
  this.d = [];
  var b = arguments.length;
  if(b > 1) {
    if(b % 2) {
      throw Error("Uneven number of arguments");
    }
    for(var c = 0;c < b;c += 2) {
      Ib(this, arguments[c], arguments[c + 1])
    }
  }else {
    if(a) {
      if(a instanceof T) {
        b = a.w();
        c = a.K()
      }else {
        b = Ba(a);
        c = Aa(a)
      }
      for(var d = 0;d < b.length;d++) {
        Ib(this, b[d], c[d])
      }
    }
  }
}
i = T.prototype;
i.c = 0;
i.R = 0;
i.K = function() {
  Jb(this);
  for(var a = [], b = 0;b < this.d.length;b++) {
    a.push(this.g[this.d[b]])
  }
  return a
};
i.w = function() {
  Jb(this);
  return this.d.concat()
};
i.clear = function() {
  this.g = {};
  this.R = this.c = this.d.length = 0
};
function Jb(a) {
  if(a.c != a.d.length) {
    for(var b = 0, c = 0;b < a.d.length;) {
      var d = a.d[b];
      if(Object.prototype.hasOwnProperty.call(a.g, d)) {
        a.d[c++] = d
      }
      b++
    }
    a.d.length = c
  }
  if(a.c != a.d.length) {
    var f = {};
    for(c = b = 0;b < a.d.length;) {
      d = a.d[b];
      if(!Object.prototype.hasOwnProperty.call(f, d)) {
        a.d[c++] = d;
        f[d] = 1
      }
      b++
    }
    a.d.length = c
  }
}
function Kb(a, b, c) {
  if(Object.prototype.hasOwnProperty.call(a.g, b)) {
    return a.g[b]
  }
  return c
}
function Ib(a, b, c) {
  if(!Object.prototype.hasOwnProperty.call(a.g, b)) {
    a.c++;
    a.d.push(b);
    a.R++
  }
  a.g[b] = c
}
T.prototype.U = function(a) {
  Jb(this);
  var b = 0, c = this.d, d = this.g, f = this.R, e = this, g = new Fb;
  g.next = function() {
    for(;;) {
      if(f != e.R) {
        throw Error("The map has changed since the iterator was created");
      }
      if(b >= c.length) {
        throw Eb;
      }
      var h = c[b++];
      return a ? h : d[h]
    }
  };
  return g
};function Lb(a) {
  return Mb(a || arguments.callee.caller, [])
}
function Mb(a, b) {
  var c = [];
  if(C(b, a) >= 0) {
    c.push("[...circular reference...]")
  }else {
    if(a && b.length < 50) {
      c.push(Nb(a) + "(");
      for(var d = a.arguments, f = 0;f < d.length;f++) {
        f > 0 && c.push(", ");
        var e;
        e = d[f];
        switch(typeof e) {
          case "object":
            e = e ? "object" : "null";
            break;
          case "string":
            e = e;
            break;
          case "number":
            e = String(e);
            break;
          case "boolean":
            e = e ? "true" : "false";
            break;
          case "function":
            e = (e = Nb(e)) ? e : "[fn]";
            break;
          case "undefined":
          ;
          default:
            e = typeof e;
            break
        }
        if(e.length > 40) {
          e = e.substr(0, 40) + "..."
        }
        c.push(e)
      }
      b.push(a);
      c.push(")\n");
      try {
        c.push(Mb(a.caller, b))
      }catch(g) {
        c.push("[exception trying to get caller]\n")
      }
    }else {
      a ? c.push("[...long stack...]") : c.push("[end]")
    }
  }
  return c.join("")
}
function Nb(a) {
  a = String(a);
  if(!Ob[a]) {
    var b = /function ([^\(]+)/.exec(a);
    Ob[a] = b ? b[1] : "[Anonymous]"
  }
  return Ob[a]
}
var Ob = {};function U(a, b, c, d, f) {
  this.reset(a, b, c, d, f)
}
U.prototype.Qa = 0;
U.prototype.qa = null;
U.prototype.pa = null;
var Pb = 0;
U.prototype.reset = function(a, b, c, d, f) {
  this.Qa = typeof f == "number" ? f : Pb++;
  this.ab = d || ga();
  this.B = a;
  this.Za = b;
  this.Xa = c;
  delete this.qa;
  delete this.pa
};
U.prototype.Aa = function(a) {
  this.B = a
};function V(a) {
  this.Na = a
}
V.prototype.O = null;
V.prototype.B = null;
V.prototype.V = null;
V.prototype.ra = null;
function W(a, b) {
  this.name = a;
  this.value = b
}
W.prototype.toString = function() {
  return this.name
};
var Qb = new W("SEVERE", 1E3), Rb = new W("WARNING", 900), Sb = new W("CONFIG", 700), Tb = new W("FINE", 500), Ub = new W("FINEST", 300);
V.prototype.Aa = function(a) {
  this.B = a
};
function Vb(a) {
  if(a.B) {
    return a.B
  }
  if(a.O) {
    return Vb(a.O)
  }
  ra("Root logger has no level set.");
  return null
}
V.prototype.log = function(a, b, c) {
  if(a.value >= Vb(this).value) {
    a = this.Ma(a, b, c);
    for(b = this;b;) {
      c = b;
      if(c.ra) {
        for(var d = 0, f = void 0;f = c.ra[d];d++) {
          f(a)
        }
      }
      b = b.O
    }
  }
};
V.prototype.Ma = function(a, b, c) {
  var d = new U(a, String(b), this.Na);
  if(c) {
    d.qa = c;
    var f;
    var e = arguments.callee.caller;
    try {
      var g, h = ba("window.location.href");
      g = typeof c == "string" ? {message:c, name:"Unknown error", lineNumber:"Not available", fileName:h, stack:"Not available"} : !c.lineNumber || !c.fileName || !c.stack ? {message:c.message, name:c.name, lineNumber:c.lineNumber || c.Wa || "Not available", fileName:c.fileName || c.filename || c.sourceURL || h, stack:c.stack || "Not available"} : c;
      f = "Message: " + z(g.message) + '\nUrl: <a href="view-source:' + g.fileName + '" target="_new">' + g.fileName + "</a>\nLine: " + g.lineNumber + "\n\nBrowser stack:\n" + z(g.stack + "-> ") + "[end]\n\nJS stack traversal:\n" + z(Lb(e) + "-> ")
    }catch(j) {
      f = "Exception trying to expose exception! You win, we lose. " + j
    }
    d.pa = f
  }
  return d
};
function X(a, b, c) {
  a.log(Tb, b, c)
}
var Wb = {}, Xb = null;
function Yb(a) {
  if(!Xb) {
    Xb = new V("");
    Wb[""] = Xb;
    Xb.Aa(Sb)
  }
  var b;
  if(!(b = Wb[a])) {
    b = new V(a);
    var c = a.lastIndexOf("."), d = a.substr(0, c);
    c = a.substr(c + 1);
    d = Yb(d);
    if(!d.V) {
      d.V = {}
    }
    d.V[c] = b;
    b.O = d;
    b = Wb[a] = b
  }
  return b
}
;function Zb() {
}
Zb.prototype.I = null;function $b() {
  return ac(bc)
}
var bc;
function cc() {
}
y(cc, Zb);
function ac(a) {
  return(a = dc(a)) ? new ActiveXObject(a) : new XMLHttpRequest
}
function ec(a) {
  var b = {};
  if(dc(a)) {
    b[0] = true;
    b[1] = true
  }
  return b
}
cc.prototype.ca = null;
function dc(a) {
  if(!a.ca && typeof XMLHttpRequest == "undefined" && typeof ActiveXObject != "undefined") {
    for(var b = ["MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "Microsoft.XMLHTTP"], c = 0;c < b.length;c++) {
      var d = b[c];
      try {
        new ActiveXObject(d);
        return a.ca = d
      }catch(f) {
      }
    }
    throw Error("Could not create ActiveXObject. ActiveX might be disabled, or MSXML might not be installed");
  }
  return a.ca
}
bc = new cc;function fc() {
  if(Ma) {
    this.i = {};
    this.T = {};
    this.Q = []
  }
}
fc.prototype.b = Yb("goog.net.xhrMonitor");
fc.prototype.J = Ma;
function gc(a, b) {
  if(a.J) {
    var c = s(b) ? b : da(b) ? t(b) : "";
    a.b.log(Ub, "Pushing context: " + b + " (" + c + ")", void 0);
    a.Q.push(c)
  }
}
function hc(a) {
  if(a.J) {
    var b = a.Q.pop();
    a.b.log(Ub, "Popping context: " + b, void 0);
    ic(a, b)
  }
}
function jc(a, b) {
  if(a.J) {
    var c = t(b);
    X(a.b, "Opening XHR : " + c);
    for(var d = 0;d < a.Q.length;d++) {
      var f = a.Q[d];
      kc(a, a.i, f, c);
      kc(a, a.T, c, f)
    }
  }
}
function ic(a, b) {
  var c = a.T[b], d = a.i[b];
  if(c && d) {
    a.b.log(Ub, "Updating dependent contexts", void 0);
    D(c, function(f) {
      D(d, function(e) {
        kc(this, this.i, f, e);
        kc(this, this.T, e, f)
      }, this)
    }, a)
  }
}
function kc(a, b, c, d) {
  b[c] || (b[c] = []);
  C(b[c], d) >= 0 || b[c].push(d)
}
var Y = new fc;function Z(a) {
  this.headers = new T;
  this.q = a || null
}
y(Z, Ab);
Z.prototype.b = Yb("goog.net.XhrIo");
var lc = [];
function mc(a, b, c, d, f, e) {
  var g = new Z;
  lc.push(g);
  b && rb(g, "complete", b);
  rb(g, "ready", fa(nc, g));
  if(e) {
    g.H = Math.max(0, e)
  }
  g.send(a, c, d, f)
}
function nc(a) {
  a.u();
  sa(lc, a)
}
i = Z.prototype;
i.h = false;
i.a = null;
i.S = null;
i.ua = "";
i.ta = "";
i.z = 0;
i.A = "";
i.Y = false;
i.L = false;
i.da = false;
i.l = false;
i.H = 0;
i.m = null;
i.send = function(a, b, c, d) {
  if(this.h) {
    throw Error("[goog.net.XhrIo] Object is active with another request");
  }
  b = b || "GET";
  this.ua = a;
  this.A = "";
  this.z = 0;
  this.ta = b;
  this.Y = false;
  this.h = true;
  this.a = this.q ? ac(this.q) : new $b;
  this.S = this.q ? this.q.I || (this.q.I = ec(this.q)) : bc.I || (bc.I = ec(bc));
  jc(Y, this.a);
  this.a.onreadystatechange = v(this.ya, this);
  try {
    X(this.b, $(this, "Opening Xhr"));
    this.da = true;
    this.a.open(b, a, true);
    this.da = false
  }catch(f) {
    X(this.b, $(this, "Error opening Xhr: " + f.message));
    oc(this, 5, f);
    return
  }
  a = c || "";
  var e = new T(this.headers);
  d && Db(d, function(h, j) {
    Ib(e, j, h)
  });
  b == "POST" && !Object.prototype.hasOwnProperty.call(e.g, "Content-Type") && Ib(e, "Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
  Db(e, function(h, j) {
    this.a.setRequestHeader(j, h)
  }, this);
  try {
    if(this.m) {
      Bb.clearTimeout(this.m);
      this.m = null
    }
    if(this.H > 0) {
      X(this.b, $(this, "Will abort after " + this.H + "ms if incomplete"));
      this.m = Bb.setTimeout(v(this.Sa, this), this.H)
    }
    X(this.b, $(this, "Sending request"));
    this.L = true;
    this.a.send(a);
    this.L = false
  }catch(g) {
    X(this.b, $(this, "Send error: " + g.message));
    oc(this, 5, g)
  }
};
i.dispatchEvent = function(a) {
  if(this.a) {
    gc(Y, this.a);
    try {
      return Z.p.dispatchEvent.call(this, a)
    }finally {
      hc(Y)
    }
  }else {
    return Z.p.dispatchEvent.call(this, a)
  }
};
i.Sa = function() {
  if(typeof aa != "undefined") {
    if(this.a) {
      this.A = "Timed out after " + this.H + "ms, aborting";
      this.z = 8;
      X(this.b, $(this, this.A));
      this.dispatchEvent("timeout");
      this.abort(8)
    }
  }
};
function oc(a, b, c) {
  a.h = false;
  if(a.a) {
    a.l = true;
    a.a.abort();
    a.l = false
  }
  a.A = c;
  a.z = b;
  pc(a);
  qc(a)
}
function pc(a) {
  if(!a.Y) {
    a.Y = true;
    a.dispatchEvent("complete");
    a.dispatchEvent("error")
  }
}
Z.prototype.abort = function(a) {
  if(this.a) {
    X(this.b, $(this, "Aborting"));
    this.h = false;
    this.l = true;
    this.a.abort();
    this.l = false;
    this.z = a || 7;
    this.dispatchEvent("complete");
    this.dispatchEvent("abort");
    qc(this)
  }
};
Z.prototype.f = function() {
  if(this.a) {
    if(this.h) {
      this.h = false;
      this.l = true;
      this.a.abort();
      this.l = false
    }
    qc(this, true)
  }
  Z.p.f.call(this)
};
Z.prototype.ya = function() {
  !this.da && !this.L && !this.l ? this.ea() : rc(this)
};
Z.prototype.ea = function() {
  rc(this)
};
function rc(a) {
  if(a.h) {
    if(typeof aa != "undefined") {
      if(a.S[1] && sc(a) == 4 && tc(a) == 2) {
        X(a.b, $(a, "Local request error detected and ignored"))
      }else {
        if(a.L && sc(a) == 4) {
          Bb.setTimeout(v(a.ya, a), 0)
        }else {
          a.dispatchEvent("readystatechange");
          if(sc(a) == 4) {
            X(a.b, $(a, "Request complete"));
            a.h = false;
            var b;
            a:switch(tc(a)) {
              case 0:
              ;
              case 200:
              ;
              case 204:
              ;
              case 304:
                b = true;
                break a;
              default:
                b = false;
                break a
            }if(b) {
              a.dispatchEvent("complete");
              a.dispatchEvent("success")
            }else {
              a.z = 6;
              var c;
              try {
                c = sc(a) > 2 ? a.a.statusText : ""
              }catch(d) {
                X(a.b, "Can not get status: " + d.message);
                c = ""
              }
              a.A = c + " [" + tc(a) + "]";
              pc(a)
            }
            qc(a)
          }
        }
      }
    }
  }
}
function qc(a, b) {
  if(a.a) {
    var c = a.a, d = a.S[0] ? p : null;
    a.a = null;
    a.S = null;
    if(a.m) {
      Bb.clearTimeout(a.m);
      a.m = null
    }
    if(!b) {
      gc(Y, c);
      a.dispatchEvent("ready");
      hc(Y)
    }
    if(Y.J) {
      var f = t(c);
      X(Y.b, "Closing XHR : " + f);
      delete Y.T[f];
      for(var e in Y.i) {
        sa(Y.i[e], f);
        Y.i[e].length == 0 && delete Y.i[e]
      }
    }
    try {
      c.onreadystatechange = d
    }catch(g) {
      a.b.log(Qb, "Problem encountered resetting onreadystatechange: " + g.message, void 0)
    }
  }
}
function sc(a) {
  return a.a ? a.a.readyState : 0
}
function tc(a) {
  try {
    return sc(a) > 2 ? a.a.status : -1
  }catch(b) {
    a.b.log(Rb, "Can not get status: " + b.message, void 0);
    return-1
  }
}
function $(a, b) {
  return b + " [" + a.ta + " " + a.ua + " " + tc(a) + "]"
}
$a[$a.length] = function(a) {
  Z.prototype.ea = a.Ua(Z.prototype.ea)
};function uc(a, b, c) {
  for(var d = J("ul", "reasons"), f = 0, e;e = c.reasons[f];f++) {
    var g = J("li"), h = J("span", "action");
    e.action == "++" ? E(h, "plusplus") : F(h, "plusplus");
    e.action != "++" ? E(h, "minusminus") : F(h, "minusminus");
    h.innerHTML = e.action;
    g.appendChild(h);
    h = J("span", {}, "'ed by ");
    g.appendChild(h);
    h = J("span", "sender", e.sender);
    g.appendChild(h);
    h = e.reason.toLowerCase().indexOf(a.toLowerCase() + e.action);
    if(h != -1) {
      h = e.reason.substring(h + a.length + 2);
      if(!/^[\s\xa0]*$/.test(h == null ? "" : String(h))) {
        h = J("span", {title:e.reason}, h);
        g.appendChild(h)
      }
    }
    e = new Date(e.timestampMsec);
    e = J("span", "date", " on " + ((e.getMonth() + 1 < 10 ? "0" + (e.getMonth() + 1) : e.getMonth() + 1) + "/" + (e.getDate() < 10 ? "0" + e.getDate() : e.getDate()) + "/" + e.getFullYear()));
    g.appendChild(e);
    d.appendChild(g)
  }
  a = J("img", {src:c.graph});
  d = J("div", "target-details", a, d);
  b.appendChild(d)
}
o("addTargetDetails", uc, void 0);
o("toggleTargetDetails", function(a, b, c) {
  var d = a.parentNode, f = d.parentNode;
  ya(f, "target-expanded");
  if(Va(document, "div", "target-details", d).length == 0) {
    E(f, "target-loading");
    mc("/targetdetailsjson/" + b + "/" + c, function(e) {
      F(f, "target-loading");
      var g;
      e = e.target;
      if(e.a) {
        b: {
          e = String(e.a.responseText);
          if(/^\s*$/.test(e) ? false : /^[\],:{}\s\u2028\u2029]*$/.test(e.replace(/\\["\\\/bfnrtu]/g, "@").replace(/"[^"\\\n\r\u2028\u2029\x00-\x08\x10-\x1f\x80-\x9f]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, "]").replace(/(?:^|:|,)(?:[\s\u2028\u2029]*\[)+/g, ""))) {
            try {
              g = eval("(" + e + ")");
              break b
            }catch(h) {
            }
          }
          throw Error("Invalid JSON string: " + e);
        }
      }
      uc(c, d, g)
    })
  }
}, void 0);
function vc(a, b) {
  this.s = a;
  this.G = b;
  this.D = 1;
  this.Ba()
}
vc.prototype.Ba = function() {
  if(this.D == 1) {
    this.G.reverse();
    wc(this)
  }else {
    this.D = 1;
    this.r = "&#8679;";
    this.G.sort(function(a, b) {
      return a.name.localeCompare(b.name)
    })
  }
  xc(this)
};
function wc(a) {
  a.r = a.r == "&#8681;" ? "&#8679;" : "&#8681;"
}
vc.prototype.Ra = function() {
  if(this.D == 2) {
    this.G.reverse();
    wc(this)
  }else {
    this.D = 2;
    this.r = "&#8681;";
    this.G.sort(function(a, b) {
      return b.score - a.score
    })
  }
  xc(this)
};
function xc(a) {
  var b = I("score-table"), c = {s:a.s, Ea:a.G}, d = '<table class="channel-table"><tr><th class="target-cell" id="target-name-header" style="cursor: pointer; cursor: hand">Target</th><th class="score-cell" id="target-score-header" style="cursor: pointer; cursor: hand">Score</th></tr>';
  if(c.Ea.length == 0) {
    d += "<tr><td>No scores yet! Start ++'ing and --'ing stuff!</td></tr>"
  }else {
    for(var f = c.Ea, e = f.length, g = 0;g < e;g++) {
      d = d;
      var h;
      h = {s:c.s, target:f[g]};
      h = '<tr><td class="target-cell"><div class="target-name" onclick="toggleTargetDetails(this, \'' + z(String(h.s)) + "', '" + z(String(h.target.name)) + "')\">" + z(String(h.target.name)) + '</div></td><td class="score-cell">' + z(String(h.target.score)) + "</td></tr>";
      d = d + h
    }
  }
  d += "</table>";
  b.innerHTML = d;
  b = I("target-name-header");
  c = I("target-score-header");
  b.onclick = v(a.Ba, a);
  c.onclick = v(a.Ra, a);
  if(a.D == 1) {
    b.innerHTML = a.r + b.innerHTML
  }else {
    c.innerHTML = a.r + c.innerHTML
  }
}
o("partychapp.ScoreTable", vc, void 0);function yc() {
  this.v = [];
  this.fa = new T;
  this.Ha = this.Ia = this.Ja = this.Ca = 0;
  this.F = new T;
  this.ja = this.Ga = 0;
  this.Oa = 1;
  this.Z = new N(0, 4E3);
  this.Z.t = function() {
    return new zc
  };
  this.Da = new N(0, 50);
  this.Da.t = function() {
    return new Ac
  };
  var a = this;
  this.ba = new N(0, 2E3);
  this.ba.t = function() {
    return String(a.Oa++)
  };
  this.ba.X = function() {
  };
  this.La = 3
}
yc.prototype.b = Yb("goog.debug.Trace");
function Ac() {
  this.ha = this.Fa = this.W = 0
}
Ac.prototype.toString = function() {
  var a = [];
  a.push(this.type, " ", this.W, " (", Math.round(this.Fa * 10) / 10, " ms)");
  this.ha && a.push(" [VarAlloc = ", this.ha, "]");
  return a.join("")
};
function zc() {
}
function Bc(a, b, c, d) {
  var f = [];
  c == -1 ? f.push("    ") : f.push(Cc(a.na - c));
  f.push(" ", Dc(a.na - b));
  if(a.$ == 0) {
    f.push(" Start        ")
  }else {
    if(a.$ == 1) {
      f.push(" Done ");
      f.push(Cc(a.$a - a.startTime), " ms ")
    }else {
      f.push(" Comment      ")
    }
  }
  f.push(d, a);
  a.Ta > 0 && f.push("[VarAlloc ", a.Ta, "] ");
  return f.join("")
}
zc.prototype.toString = function() {
  return this.type == null ? this.Ka : "[" + this.type + "] " + this.Ka
};
yc.prototype.reset = function(a) {
  this.La = a;
  for(a = 0;a < this.v.length;a++) {
    var b = this.Z.id;
    b && P(this.ba, b);
    P(this.Z, this.v[a])
  }
  this.v.length = 0;
  this.fa.clear();
  this.Ca = ga();
  this.ja = this.Ga = this.Ha = this.Ia = this.Ja = 0;
  b = this.F.w();
  for(a = 0;a < b.length;a++) {
    var c = Kb(this.F, b[a]);
    c.W = 0;
    c.Fa = 0;
    c.ha = 0;
    P(this.Da, c)
  }
  this.F.clear()
};
yc.prototype.toString = function() {
  for(var a = [], b = -1, c = [], d = 0;d < this.v.length;d++) {
    var f = this.v[d];
    f.$ == 1 && c.pop();
    a.push(" ", Bc(f, this.Ca, b, c.join("")));
    b = f.na;
    a.push("\n");
    f.$ == 0 && c.push("|  ")
  }
  if(this.fa.c != 0) {
    var e = ga();
    a.push(" Unstopped timers:\n");
    Hb(this.fa, function(g) {
      a.push("  ", g, " (", e - g.startTime, " ms, started at ", Dc(g.startTime), ")\n")
    })
  }
  b = this.F.w();
  for(d = 0;d < b.length;d++) {
    c = Kb(this.F, b[d]);
    c.W > 1 && a.push(" TOTAL ", c, "\n")
  }
  a.push("Total tracers created ", this.Ga, "\n", "Total comments created ", this.ja, "\n", "Overhead start: ", this.Ja, " ms\n", "Overhead end: ", this.Ia, " ms\n", "Overhead comment: ", this.Ha, " ms\n");
  return a.join("")
};
function Cc(a) {
  a = Math.round(a);
  var b = "";
  if(a < 1E3) {
    b = " "
  }
  if(a < 100) {
    b = "  "
  }
  if(a < 10) {
    b = "   "
  }
  return b + a
}
function Dc(a) {
  a = Math.round(a);
  return String(100 + a / 1E3 % 60).substring(1, 3) + "." + String(1E3 + a % 1E3).substring(1, 4)
}
new yc;new N(0, 100);o("showCreateForm", function() {
  E(I("create-button-container"), "hidden");
  F(I("create-table"), "hidden")
}, void 0);
o("submitCreateRoom", function() {
  var a = I("room-name").value, b = I("inviteonly-true").checked, c = I("invitees").value;
  if(/^[\s\xa0]*$/.test(a == null ? "" : String(a))) {
    alert("Please enter a room name.");
    return false
  }
  mc("/channel/create", function(d) {
    var f = I("create-result");
    F(f, "hidden");
    f.innerHTML = d.target.a ? d.target.a.responseText : ""
  }, "POST", "name=" + encodeURIComponent(a) + "&inviteonly=" + b + "&invitees=" + encodeURIComponent(c));
  return false
}, void 0);
o("acceptInvitation", function(a) {
  window.location.href = "/channel/invitation/accept?name=" + encodeURIComponent(a)
}, void 0);
o("declineInvitation", function(a) {
  window.location.href = "/channel/invitation/decline?name=" + encodeURIComponent(a)
}, void 0);
o("requestInvitation", function(a) {
  window.location.href = "/channel/invitation/request?name=" + encodeURIComponent(a)
}, void 0);
o("getInvitation", function(a) {
  window.location.href = "/channel/invitation/get?name=" + encodeURIComponent(a)
}, void 0);
o("displayChannels", function(a, b) {
  b.setAttribute("style", "display: block");
  if(a.error) {
    b.innerHTML = "ERROR: " + a.error
  }else {
    for(var c = J("ul", "channel-list"), d = a.channels, f = 0, e;e = d[f];f++) {
      var g = J("a", {href:"/channel/" + e.name}, e.name);
      e = J("span", "description", " as ", J("b", {}, e.Va), e.wa > 1 ? " with " + (e.wa - 1) + (e.wa == 2 ? " other" : " others") : "");
      g = J("li", {}, g, e);
      c.appendChild(g)
    }
    b.appendChild(c)
  }
}, void 0);
o("printEmail", function(a) {
  for(var b = [112, 97, 114, 116, 121, 99, 104, 97, 112, 112, 64, 103, 111, 111, 103, 108, 101, 103, 114, 111, 117, 112, 115, 46, 99, 111, 109], c = [], d = 0;d < b.length;d++) {
    c.push(String.fromCharCode(b[d]))
  }
  c = c.join("");
  document.write('<a href="mailto:' + c + '">' + (a || c) + "</a>")
}, void 0);
