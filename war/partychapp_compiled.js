function showCreateForm() {
  goog.dom.classes.add(goog.dom.$("create-button-container"), "hidden");
  goog.dom.classes.remove(goog.dom.$("create-table"), "hidden")
}
function submitCreateRoom() {
  var i = goog.dom.$("room-name").value, k = goog.dom.$("inviteonly-true").checked, m = goog.dom.$("invitees").value;
  if(goog.string.isEmptySafe(i)) {
    alert("Please enter a room name.");
    return false
  }
  goog.net.XhrIo.send("/channel/create", function(o) {
    var q = goog.dom.$("create-result");
    goog.dom.classes.remove(q, "hidden");
    q.innerHTML = o.target.getResponseText()
  }, "POST", "name=" + encodeURIComponent(i) + "&inviteonly=" + k + "&invitees=" + encodeURIComponent(m));
  return false
}
function acceptInvitation(i) {
  window.location.href = "/channel/invitation/accept?name=" + encodeURIComponent(i)
}
function declineInvitation(i) {
  window.location.href = "/channel/invitation/decline?name=" + encodeURIComponent(i)
}
function requestInvitation(i) {
  window.location.href = "/channel/invitation/request?name=" + encodeURIComponent(i)
}
function getInvitation(i) {
  window.location.href = "/channel/invitation/get?name=" + encodeURIComponent(i)
}
function formatDate(i) {
  function k(m) {
    return m < 10 ? "0" + m : m
  }
  return k(i.getMonth() + 1) + "/" + k(i.getDate()) + "/" + i.getFullYear()
}
function addTargetDetails(i, k, m) {
  for(var o = goog.dom.$dom("ul", "reasons"), q = 0, u;u = m.reasons[q];q++) {
    var A = goog.dom.$dom("li"), p = goog.dom.$dom("span", "action");
    goog.dom.classes.enable(p, "plusplus", u.action == "++");
    goog.dom.classes.enable(p, "minusminus", u.action != "++");
    p.innerHTML = u.action;
    A.appendChild(p);
    p = goog.dom.$dom("span", {}, "'ed by ");
    A.appendChild(p);
    p = goog.dom.$dom("span", "sender", u.sender);
    A.appendChild(p);
    p = u.reason.toLowerCase().indexOf(i.toLowerCase() + u.action);
    if(p != -1) {
      p = u.reason.substring(p + i.length + 2);
      if(!goog.string.isEmptySafe(p)) {
        p = goog.dom.$dom("span", {title:u.reason}, p);
        A.appendChild(p)
      }
    }
    u = goog.dom.$dom("span", "date", " on " + formatDate(new Date(u.timestampMsec)));
    A.appendChild(u);
    o.appendChild(A)
  }
  i = goog.dom.$dom("img", {src:m.graph});
  o = goog.dom.$dom("div", "target-details", i, o);
  k.appendChild(o)
}
function toggleTargetDetails(i, k, m) {
  var o = i.parentNode, q = o.parentNode;
  goog.dom.classes.toggle(q, "target-expanded");
  if(goog.dom.$$("div", "target-details", o).length == 0) {
    goog.dom.classes.add(q, "target-loading");
    goog.net.XhrIo.send("/targetdetailsjson/" + k + "/" + m, function(u) {
      goog.dom.classes.remove(q, "target-loading");
      addTargetDetails(m, o, u.target.getResponseJson())
    })
  }
}
function displayChannels(i, k) {
  k.setAttribute("style", "display: block");
  if(i.error) {
    targetDiv.innerHTML = "ERROR: " + i.error
  }else {
    for(var m = goog.dom.$dom("ul", "channel-list"), o = i.channels, q = 0, u;u = o[q];q++) {
      var A = goog.dom.$dom("a", {href:"/channel/" + u.name}, u.name);
      u = goog.dom.$dom("span", "description", " as ", goog.dom.$dom("b", {}, u.alias), u.memberCount > 1 ? " with " + (u.memberCount - 1) + (u.memberCount == 2 ? " other" : " others") : "");
      A = goog.dom.$dom("li", {}, A, u);
      m.appendChild(A)
    }
    k.appendChild(m)
  }
}
function printEmail(i) {
  for(var k = [112, 97, 114, 116, 121, 99, 104, 97, 112, 112, 64, 103, 111, 111, 103, 108, 101, 103, 114, 111, 117, 112, 115, 46, 99, 111, 109], m = [], o = 0;o < k.length;o++) {
    m.push(String.fromCharCode(k[o]))
  }
  m = m.join("");
  document.write('<a href="mailto:' + m + '">' + (i || m) + "</a>")
}
var SortOrder = {BY_NAME:1, BY_SCORE:2}, UP_ARROW = "&#8679;", DOWN_ARROW = "&#8681;";
function ScoreTable(i, k) {
  this.channelName = i;
  this.targetList = k;
  this.sortOrder = undefined;
  this.sortByName()
}
ScoreTable.prototype.sortByName = function() {
  if(this.sortOrder == SortOrder.BY_NAME) {
    this.targetList.reverse();
    this.toggleArrow()
  }else {
    this.sortOrder = SortOrder.BY_NAME;
    this.arrow = UP_ARROW;
    this.targetList.sort(function(i, k) {
      return i.name.localeCompare(k.name)
    })
  }
  this.draw()
};
ScoreTable.prototype.toggleArrow = function() {
  this.arrow = this.arrow == DOWN_ARROW ? UP_ARROW : DOWN_ARROW
};
ScoreTable.prototype.sortByScore = function() {
  if(this.sortOrder == SortOrder.BY_SCORE) {
    this.targetList.reverse();
    this.toggleArrow()
  }else {
    this.sortOrder = SortOrder.BY_SCORE;
    this.arrow = DOWN_ARROW;
    this.targetList.sort(function(i, k) {
      return k.score - i.score
    })
  }
  this.draw()
};
ScoreTable.prototype.draw = function() {
  soy.renderElement(goog.dom.$("score-table"), partychapp.templates.scoreTable, {channelName:this.channelName, targets:this.targetList});
  var i = goog.dom.$("target-name-header"), k = goog.dom.$("target-score-header");
  i.onclick = goog.bind(this.sortByName, this);
  k.onclick = goog.bind(this.sortByScore, this);
  if(this.sortOrder == SortOrder.BY_NAME) {
    i.innerHTML = this.arrow + i.innerHTML
  }else {
    k.innerHTML = this.arrow + k.innerHTML
  }
};(function() {
  function i() {
    return function() {
    }
  }
  function k(a) {
    return function(c) {
      this[a] = c
    }
  }
  function m(a) {
    return function() {
      return this[a]
    }
  }
  function o() {
  }
  function q(a, c) {
    this.x = b.Oa(a) ? a : 0;
    this.y = b.Oa(c) ? c : 0
  }
  function u(a, c) {
    var d = a.x - c.x;
    a = a.y - c.y;
    return Math.sqrt(d * d + a * a)
  }
  function A(a, c) {
    return new q(a.x - c.x, a.y - c.y)
  }
  function p(a, c) {
    this.width = a;
    this.height = c
  }
  function n(a) {
    this.pa = a || b.global.document || document
  }
  function H(a, c) {
    this.type = a;
    this.currentTarget = this.target = c
  }
  function s(a, c) {
    a && this.bb(a, c)
  }
  function L() {
  }
  function B(a, c) {
    this.Rf = c;
    this.Xa = [];
    if(a > this.Rf) {
      throw Error("[goog.structs.SimplePool] Initial cannot be greater than max");
    }
    for(c = 0;c < a;c++) {
      this.Xa.push(this.wd())
    }
  }
  function C(a) {
    this.Qb = a
  }
  function ba(a, c) {
    if(a.m) {
      a.m[c] = true
    }else {
      if(a.Qa) {
        a.m = ca.ta();
        a.m[a.Qa] = true;
        a.Qa = null;
        a.m[c] = true
      }else {
        a.Qa = c
      }
    }
  }
  function G() {
  }
  function da(a, c, d, e, f) {
    if(!b.userAgent.j && !(b.userAgent.ca && b.userAgent.aa("525"))) {
      return true
    }
    if(b.userAgent.dc && f) {
      return P(a)
    }
    if(f && !e) {
      return false
    }
    if(b.userAgent.j && !d && (c == 17 || c == 18)) {
      return false
    }
    if(b.userAgent.j && e && c == a) {
      return false
    }
    switch(a) {
      case 13:
        return true;
      case 27:
        return!b.userAgent.ca
    }
    return P(a)
  }
  function P(a) {
    if(a >= 48 && a <= 57) {
      return true
    }
    if(a >= 96 && a <= 106) {
      return true
    }
    if(a >= 65 && a <= 90) {
      return true
    }
    switch(a) {
      case 32:
      ;
      case 63:
      ;
      case 107:
      ;
      case 109:
      ;
      case 110:
      ;
      case 111:
      ;
      case 186:
      ;
      case 189:
      ;
      case 187:
      ;
      case 188:
      ;
      case 190:
      ;
      case 191:
      ;
      case 192:
      ;
      case 222:
      ;
      case 219:
      ;
      case 220:
      ;
      case 221:
        return true;
      default:
        return false
    }
  }
  function K(a) {
    a && this.De(a)
  }
  function V(a, c, d, e) {
    e && this.bb(e, undefined);
    this.type = "key";
    this.keyCode = a;
    this.charCode = c;
    this.repeat = d
  }
  function O() {
  }
  function W(a, c, d) {
    switch(typeof c) {
      case "string":
        ea(a, c, d);
        break;
      case "number":
        d.push(isFinite(c) && !isNaN(c) ? c : "null");
        break;
      case "boolean":
        d.push(c);
        break;
      case "undefined":
        d.push("null");
        break;
      case "object":
        if(c == null) {
          d.push("null");
          break
        }
        if(b.ia(c)) {
          var e = c.length;
          d.push("[");
          for(var f = "", g = 0;g < e;g++) {
            d.push(f);
            W(a, c[g], d);
            f = ","
          }
          d.push("]");
          break
        }
        d.push("{");
        e = "";
        for(f in c) {
          if(c.hasOwnProperty(f)) {
            g = c[f];
            if(typeof g != "function") {
              d.push(e);
              ea(a, f, d);
              d.push(":");
              W(a, g, d);
              e = ","
            }
          }
        }
        d.push("}");
        break;
      case "function":
        break;
      default:
        throw Error("Unknown type: " + typeof c);
    }
  }
  function ea(a, c, d) {
    d.push('"', c.replace(ta, function(e) {
      if(e in X) {
        return X[e]
      }
      var f = e.charCodeAt(0), g = "\\u";
      if(f < 16) {
        g += "000"
      }else {
        if(f < 256) {
          g += "00"
        }else {
          if(f < 4096) {
            g += "0"
          }
        }
      }
      return X[e] = g + f.toString(16)
    }), '"')
  }
  function z(a, c, d, e) {
    this.top = a;
    this.right = c;
    this.bottom = d;
    this.left = e
  }
  function fa(a, c) {
    if(!a || !c) {
      return false
    }
    if(c instanceof z) {
      return c.left >= a.left && c.right <= a.right && c.top >= a.top && c.bottom <= a.bottom
    }
    return c.x >= a.left && c.x <= a.right && c.y >= a.top && c.y <= a.bottom
  }
  function w(a, c, d, e) {
    this.left = a;
    this.top = c;
    this.width = d;
    this.height = e
  }
  function ga(a, c) {
    var d = Math.max(a.left, c.left), e = Math.min(a.left + a.width, c.left + c.width);
    if(d <= e) {
      var f = Math.max(a.top, c.top);
      a = Math.min(a.top + a.height, c.top + c.height);
      if(f <= a) {
        return new w(d, f, e - d, a - f)
      }
    }
    return null
  }
  function ha(a, c) {
    var d = Math.max(a.left, c.left);
    if(d <= Math.min(a.left + a.width, c.left + c.width)) {
      d = Math.max(a.top, c.top);
      a = Math.min(a.top + a.height, c.top + c.height);
      if(d <= a) {
        return true
      }
    }
    return false
  }
  function ia(a, c) {
    var d = ga(a, c);
    if(!d || !d.height || !d.width) {
      return[a.s()]
    }
    d = [];
    var e = a.top, f = a.height, g = a.left + a.width, j = a.top + a.height, l = c.left + c.width, r = c.top + c.height;
    if(c.top > a.top) {
      d.push(new w(a.left, a.top, a.width, c.top - a.top));
      e = c.top;
      f -= c.top - a.top
    }
    if(r < j) {
      d.push(new w(a.left, r, a.width, j - r));
      f = r - e
    }
    c.left > a.left && d.push(new w(a.left, e, c.left - a.left, f));
    l < g && d.push(new w(l, e, g - l, f));
    return d
  }
  function F(a, c) {
    this.nb = a || 1;
    this.Yb = c || M;
    this.pd = b.Ga(this.Qj, this);
    this.Wd = b.now()
  }
  function E() {
  }
  function v(a) {
    this.r = {};
    this.m = [];
    var c = arguments.length;
    if(c > 1) {
      if(c % 2) {
        throw Error("Uneven number of arguments");
      }
      for(var d = 0;d < c;d += 2) {
        this.sa(arguments[d], arguments[d + 1])
      }
    }else {
      a && this.Db(a)
    }
  }
  function ja(a, c) {
    return a === c
  }
  function Q(a) {
    if(a.w != a.m.length) {
      for(var c = 0, d = 0;c < a.m.length;) {
        var e = a.m[c];
        if(N(a.r, e)) {
          a.m[d++] = e
        }
        c++
      }
      a.m.length = d
    }
    if(a.w != a.m.length) {
      var f = {};
      for(d = c = 0;c < a.m.length;) {
        e = a.m[c];
        if(!N(f, e)) {
          a.m[d++] = e;
          f[e] = 1
        }
        c++
      }
      a.m.length = d
    }
  }
  function N(a, c) {
    return Object.prototype.hasOwnProperty.call(a, c)
  }
  function x(a) {
    this.r = new v;
    a && this.Db(a)
  }
  function Y(a) {
    var c = typeof a;
    return c == "object" && a || c == "function" ? "o" + b.ra(a) : c.substr(0, 1) + a
  }
  function J() {
    if(b.userAgent.la) {
      this.Ka = {};
      this.Vc = {};
      this.Sc = []
    }
  }
  function Z(a) {
    return b.l(a) ? a : b.Pa(a) ? b.ra(a) : ""
  }
  function ua(a, c) {
    var d = a.Vc[c], e = a.Ka[c];
    d && e && b.c.forEach(d, function(f) {
      b.c.forEach(e, function(g) {
        S(this, this.Ka, f, g);
        S(this, this.Vc, g, f)
      }, this)
    }, a)
  }
  function S(a, c, d, e) {
    c[d] || (c[d] = []);
    b.c.contains(c[d], e) || c[d].push(e)
  }
  function t() {
    this.headers = new v
  }
  function va(a) {
    a.oa();
    b.c.remove(T, a)
  }
  function ka(a, c, d) {
    a.Fa = false;
    if(a.o) {
      a.ab = true;
      a.o.abort();
      a.ab = false
    }
    a.cb = d;
    a.rb = c;
    la(a);
    U(a)
  }
  function la(a) {
    if(!a.Cd) {
      a.Cd = true;
      a.dispatchEvent("complete");
      a.dispatchEvent("error")
    }
  }
  function ma(a) {
    if(a.Fa) {
      if(typeof b != "undefined") {
        if(!(a.Uc[$] && a.lb() == 4 && a.Fc() == 2)) {
          if(a.Jc && a.lb() == 4) {
            M.setTimeout(b.Ga(a.Vf, a), 0)
          }else {
            a.dispatchEvent("readystatechange");
            if(a.Td()) {
              a.Fa = false;
              if(a.Jf()) {
                a.dispatchEvent("complete");
                a.dispatchEvent("success")
              }else {
                a.rb = 6;
                a.cb = a.yf() + " [" + a.Fc() + "]";
                la(a)
              }
              U(a)
            }
          }
        }
      }
    }
  }
  function U(a, c) {
    if(a.o) {
      var d = a.o, e = a.Uc[aa] ? b.db : null;
      a.o = null;
      a.Uc = null;
      if(a.fb) {
        M.clearTimeout(a.fb);
        a.fb = null
      }
      if(!c) {
        b.h.gb.be(d);
        a.dispatchEvent("ready");
        b.h.gb.ae()
      }
      b.h.gb.Of(d);
      try {
        d.onreadystatechange = e
      }catch(f) {
      }
    }
  }
  var h, b = b || {};
  b.global = this;
  b.Ab = false;
  b.zg = "en";
  b.jc = null;
  b.hj = function(a) {
    b.$e(a)
  };
  b.$e = function(a, c, d) {
    a = a.split(".");
    d = d || b.global;
    !(a[0] in d) && d.execScript && d.execScript("var " + a[0]);
    for(var e;a.length && (e = a.shift());) {
      if(!a.length && b.Oa(c)) {
        d[e] = c
      }else {
        d = d[e] ? d[e] : (d[e] = {})
      }
    }
  };
  b.tf = function(a, c) {
    a = a.split(".");
    c = c || b.global;
    for(var d;d = a.shift();) {
      if(c[d]) {
        c = c[d]
      }else {
        return null
      }
    }
    return c
  };
  b.ii = function(a, c) {
    c = c || b.global;
    for(var d in a) {
      c[d] = a[d]
    }
  };
  b.Dg = i();
  b.mj = i();
  b.ik = false;
  b.Ig = "";
  b.db = i();
  b.pi = function(a) {
    return a
  };
  b.Cg = function() {
    throw Error("unimplemented abstract method");
  };
  b.Fg = function(a) {
    a.um = function() {
      return a.ui || (a.ui = new a)
    }
  };
  b.zb = function(a) {
    var c = typeof a;
    if(c == "object") {
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
      if(c == "function" && typeof a.call == "undefined") {
        return"object"
      }
    }
    return c
  };
  b.ej = function(a, c) {
    if(c in a) {
      for(var d in a) {
        if(d == c && Object.prototype.hasOwnProperty.call(a, c)) {
          return true
        }
      }
    }
    return false
  };
  b.xm = function(a, c) {
    return a instanceof Object ? Object.prototype.propertyIsEnumerable.call(a, c) : b.ej(a, c)
  };
  b.Oa = function(a) {
    return a !== undefined
  };
  b.Ji = function(a) {
    return a === null
  };
  b.Ei = function(a) {
    return a != null
  };
  b.ia = function(a) {
    return b.zb(a) == "array"
  };
  b.ba = function(a) {
    var c = b.zb(a);
    return c == "array" || c == "object" && typeof a.length == "number"
  };
  b.Di = function(a) {
    return b.Pa(a) && typeof a.getFullYear == "function"
  };
  b.l = function(a) {
    return typeof a == "string"
  };
  b.zi = function(a) {
    return typeof a == "boolean"
  };
  b.Gf = function(a) {
    return typeof a == "number"
  };
  b.pb = function(a) {
    return b.zb(a) == "function"
  };
  b.Pa = function(a) {
    a = b.zb(a);
    return a == "object" || a == "array" || a == "function"
  };
  b.ra = function(a) {
    if(a.hasOwnProperty && a.hasOwnProperty(b.Ua)) {
      return a[b.Ua]
    }
    a[b.Ua] || (a[b.Ua] = ++b.ni);
    return a[b.Ua]
  };
  b.kj = function(a) {
    "removeAttribute" in a && a.removeAttribute(b.Ua);
    try {
      delete a[b.Ua]
    }catch(c) {
    }
  };
  b.Ua = "closure_hashCode_" + Math.floor(Math.random() * 2147483648).toString(36);
  b.ni = 0;
  b.sd = function(a) {
    var c = b.zb(a);
    if(c == "object" || c == "array") {
      if(a.s) {
        return a.s.call(a)
      }
      c = c == "array" ? [] : {};
      for(var d in a) {
        c[d] = b.sd(a[d])
      }
      return c
    }
    return a
  };
  b.Ga = function(a, c) {
    var d = c || b.global;
    if(arguments.length > 2) {
      var e = Array.prototype.slice.call(arguments, 2);
      return function() {
        var f = Array.prototype.slice.call(arguments);
        Array.prototype.unshift.apply(f, e);
        return a.apply(d, f)
      }
    }else {
      return function() {
        return a.apply(d, arguments)
      }
    }
  };
  b.Pc = function(a) {
    var c = Array.prototype.slice.call(arguments, 1);
    return function() {
      var d = Array.prototype.slice.call(arguments);
      d.unshift.apply(d, c);
      return a.apply(this, d)
    }
  };
  b.Ui = function(a, c) {
    for(var d in c) {
      a[d] = c[d]
    }
  };
  b.now = Date.now || function() {
    return+new Date
  };
  b.hi = function(a) {
    if(b.global.execScript) {
      b.global.execScript(a, "JavaScript")
    }else {
      if(b.global.eval) {
        if(b.jc == null) {
          b.global.eval("var _et_ = 1;");
          if(typeof b.global._et_ != "undefined") {
            delete b.global._et_;
            b.jc = true
          }else {
            b.jc = false
          }
        }
        if(b.jc) {
          b.global.eval(a)
        }else {
          var c = b.global.document, d = c.createElement("script");
          d.type = "text/javascript";
          d.defer = false;
          d.appendChild(c.createTextNode(a));
          c.body.appendChild(d);
          c.body.removeChild(d)
        }
      }else {
        throw Error("goog.globalEval not available");
      }
    }
  };
  b.he = true;
  b.Ah = function(a, c) {
    a += c ? "-" + c : "";
    return b.yd && a in b.yd ? b.yd[a] : a
  };
  b.sj = function(a) {
    b.yd = a
  };
  b.Nh = function(a, c) {
    c = c || {};
    for(var d in c) {
      a = a.replace(new RegExp("\\{\\$" + d + "\\}", "gi"), c[d])
    }
    return a
  };
  b.f = function(a, c, d) {
    b.$e(a, c, d)
  };
  b.jh = function(a, c, d) {
    a[c] = d
  };
  b.Ba = function(a, c) {
    function d() {
    }
    d.prototype = c.prototype;
    a.superClass_ = c.prototype;
    a.prototype = new d
  };
  b.tm = i();
  b.c = {};
  b.c.og = b.he;
  b.c.cj = function(a) {
    return a[a.length - 1]
  };
  b.c.v = Array.prototype;
  b.c.indexOf = b.c.v.indexOf ? function(a, c, d) {
    return b.c.v.indexOf.call(a, c, d)
  } : function(a, c, d) {
    d = d == null ? 0 : d < 0 ? Math.max(0, a.length + d) : d;
    if(b.l(a)) {
      if(!b.l(c) || c.length != 1) {
        return-1
      }
      return a.indexOf(c, d)
    }
    for(d = d;d < a.length;d++) {
      if(d in a && a[d] === c) {
        return d
      }
    }
    return-1
  };
  b.c.lastIndexOf = b.c.v.lastIndexOf ? function(a, c, d) {
    return b.c.v.lastIndexOf.call(a, c, d == null ? a.length - 1 : d)
  } : function(a, c, d) {
    d = d == null ? a.length - 1 : d;
    if(d < 0) {
      d = Math.max(0, a.length + d)
    }
    if(b.l(a)) {
      if(!b.l(c) || c.length != 1) {
        return-1
      }
      return a.lastIndexOf(c, d)
    }
    for(d = d;d >= 0;d--) {
      if(d in a && a[d] === c) {
        return d
      }
    }
    return-1
  };
  b.c.forEach = b.c.v.forEach ? function(a, c, d) {
    b.c.v.forEach.call(a, c, d)
  } : function(a, c, d) {
    for(var e = a.length, f = b.l(a) ? a.split("") : a, g = 0;g < e;g++) {
      g in f && c.call(d, f[g], g, a)
    }
  };
  b.c.ef = function(a, c, d) {
    var e = a.length, f = b.l(a) ? a.split("") : a;
    for(e -= 1;e >= 0;--e) {
      e in f && c.call(d, f[e], e, a)
    }
  };
  b.c.filter = b.c.v.filter ? function(a, c, d) {
    return b.c.v.filter.call(a, c, d)
  } : function(a, c, d) {
    for(var e = a.length, f = [], g = 0, j = b.l(a) ? a.split("") : a, l = 0;l < e;l++) {
      if(l in j) {
        var r = j[l];
        if(c.call(d, r, l, a)) {
          f[g++] = r
        }
      }
    }
    return f
  };
  b.c.map = b.c.v.map ? function(a, c, d) {
    return b.c.v.map.call(a, c, d)
  } : function(a, c, d) {
    for(var e = a.length, f = [], g = 0, j = b.l(a) ? a.split("") : a, l = 0;l < e;l++) {
      if(l in j) {
        f[g++] = c.call(d, j[l], l, a)
      }
    }
    return f
  };
  b.c.reduce = function(a, c, d, e) {
    if(a.reduce) {
      return e ? a.reduce(b.Ga(c, e), d) : a.reduce(c, d)
    }
    var f = d;
    b.c.forEach(a, function(g, j) {
      f = c.call(e, f, g, j, a)
    });
    return f
  };
  b.c.reduceRight = function(a, c, d, e) {
    if(a.reduceRight) {
      return e ? a.reduceRight(b.Ga(c, e), d) : a.reduceRight(c, d)
    }
    var f = d;
    b.c.ef(a, function(g, j) {
      f = c.call(e, f, g, j, a)
    });
    return f
  };
  b.c.some = b.c.v.some ? function(a, c, d) {
    return b.c.v.some.call(a, c, d)
  } : function(a, c, d) {
    for(var e = a.length, f = b.l(a) ? a.split("") : a, g = 0;g < e;g++) {
      if(g in f && c.call(d, f[g], g, a)) {
        return true
      }
    }
    return false
  };
  b.c.every = b.c.v.every ? function(a, c, d) {
    return b.c.v.every.call(a, c, d)
  } : function(a, c, d) {
    for(var e = a.length, f = b.l(a) ? a.split("") : a, g = 0;g < e;g++) {
      if(g in f && !c.call(d, f[g], g, a)) {
        return false
      }
    }
    return true
  };
  b.c.find = function(a, c, d) {
    c = b.c.Dd(a, c, d);
    return c < 0 ? null : b.l(a) ? a.charAt(c) : a[c]
  };
  b.c.Dd = function(a, c, d) {
    for(var e = a.length, f = b.l(a) ? a.split("") : a, g = 0;g < e;g++) {
      if(g in f && c.call(d, f[g], g, a)) {
        return g
      }
    }
    return-1
  };
  b.c.mh = function(a, c, d) {
    c = b.c.bf(a, c, d);
    return c < 0 ? null : b.l(a) ? a.charAt(c) : a[c]
  };
  b.c.bf = function(a, c, d) {
    var e = a.length, f = b.l(a) ? a.split("") : a;
    for(e -= 1;e >= 0;e--) {
      if(e in f && c.call(d, f[e], e, a)) {
        return e
      }
    }
    return-1
  };
  b.c.contains = function(a, c) {
    return b.c.indexOf(a, c) >= 0
  };
  b.c.z = function(a) {
    return a.length == 0
  };
  b.c.clear = function(a) {
    if(!b.ia(a)) {
      for(var c = a.length - 1;c >= 0;c--) {
        delete a[c]
      }
    }
    a.length = 0
  };
  b.c.ri = function(a, c) {
    b.c.contains(a, c) || a.push(c)
  };
  b.c.Rd = function(a, c, d) {
    b.c.splice(a, d, 0, c)
  };
  b.c.si = function(a, c, d) {
    b.Pc(b.c.splice, a, d, 0).apply(null, c)
  };
  b.c.insertBefore = function(a, c, d) {
    var e;
    arguments.length == 2 || (e = b.c.indexOf(a, d)) < 0 ? a.push(c) : b.c.Rd(a, c, e)
  };
  b.c.remove = function(a, c) {
    c = b.c.indexOf(a, c);
    var d;
    if(d = c >= 0) {
      b.c.ub(a, c)
    }
    return d
  };
  b.c.ub = function(a, c) {
    return b.c.v.splice.call(a, c, 1).length == 1
  };
  b.c.lj = function(a, c, d) {
    c = b.c.Dd(a, c, d);
    if(c >= 0) {
      b.c.ub(a, c);
      return true
    }
    return false
  };
  b.c.s = function(a) {
    if(b.ia(a)) {
      return a.concat()
    }else {
      for(var c = [], d = 0, e = a.length;d < e;d++) {
        c[d] = a[d]
      }
      return c
    }
  };
  b.c.yb = function(a) {
    if(b.ia(a)) {
      return a.concat()
    }
    return b.c.s(a)
  };
  b.c.extend = function(a) {
    for(var c = 1;c < arguments.length;c++) {
      var d = arguments[c];
      if(b.ba(d)) {
        d = b.c.yb(d);
        a.push.apply(a, d)
      }else {
        a.push(d)
      }
    }
  };
  b.c.splice = function(a) {
    return b.c.v.splice.apply(a, b.c.slice(arguments, 1))
  };
  b.c.slice = function(a, c, d) {
    return arguments.length <= 2 ? b.c.v.slice.call(a, c) : b.c.v.slice.call(a, c, d)
  };
  b.c.jj = function(a, c) {
    c = c || a;
    for(var d = {}, e = 0, f = 0;f < a.length;) {
      var g = a[f++], j = b.Pa(g) ? b.ra(g) : g;
      if(!Object.prototype.hasOwnProperty.call(d, j)) {
        d[j] = true;
        c[e++] = g
      }
    }
    c.length = e
  };
  b.c.od = function(a, c, d) {
    var e = 0, f = a.length - 1;
    for(d = d || b.c.Fb;e <= f;) {
      var g = e + f >> 1, j = d(c, a[g]);
      if(j > 0) {
        e = g + 1
      }else {
        if(j < 0) {
          f = g - 1
        }else {
          return g
        }
      }
    }
    return-(e + 1)
  };
  b.c.sort = function(a, c) {
    b.c.v.sort.call(a, c || b.c.Fb)
  };
  b.c.Kj = function(a, c) {
    for(var d = 0;d < a.length;d++) {
      a[d] = {index:d, value:a[d]}
    }
    var e = c || b.c.Fb;
    b.c.sort(a, function(f, g) {
      return e(f.value, g.value) || f.index - g.index
    });
    for(d = 0;d < a.length;d++) {
      a[d] = a[d].value
    }
  };
  b.c.Jj = function(a, c, d) {
    var e = d || b.c.Fb;
    b.c.sort(a, function(f, g) {
      return e(f[c], g[c])
    })
  };
  b.c.Ma = function(a, c, d) {
    if(!b.ba(a) || !b.ba(c) || a.length != c.length) {
      return false
    }
    var e = a.length;
    d = d || b.c.Ne;
    for(var f = 0;f < e;f++) {
      if(!d(a[f], c[f])) {
        return false
      }
    }
    return true
  };
  b.c.td = function(a, c, d) {
    return b.c.Ma(a, c, d)
  };
  b.c.Fb = function(a, c) {
    return a > c ? 1 : a < c ? -1 : 0
  };
  b.c.Ne = function(a, c) {
    return a === c
  };
  b.c.Jg = function(a, c, d) {
    d = b.c.od(a, c, d);
    if(d < 0) {
      b.c.Rd(a, c, -(d + 1));
      return true
    }
    return false
  };
  b.c.Kg = function(a, c, d) {
    c = b.c.od(a, c, d);
    return c >= 0 ? b.c.ub(a, c) : false
  };
  b.c.Lg = function(a, c) {
    for(var d = {}, e = 0;e < a.length;e++) {
      var f = a[e], g = c(f, e, a);
      if(b.Oa(g)) {
        (d[g] || (d[g] = [])).push(f)
      }
    }
    return d
  };
  b.c.repeat = function(a, c) {
    for(var d = [], e = 0;e < c;e++) {
      d[e] = a
    }
    return d
  };
  b.c.df = function() {
    for(var a = [], c = 0;c < arguments.length;c++) {
      var d = arguments[c];
      b.ia(d) ? a.push.apply(a, b.c.df.apply(null, d)) : a.push(d)
    }
    return a
  };
  b.c.rotate = function(a, c) {
    if(a.length) {
      c %= a.length;
      if(c > 0) {
        b.c.v.unshift.apply(a, a.splice(-c, c))
      }else {
        c < 0 && b.c.v.push.apply(a, a.splice(0, -c))
      }
    }
    return a
  };
  h = o.prototype;
  h.Bd = false;
  h.Df = m("Bd");
  h.Bh = o.prototype.Df;
  h.oa = function() {
    if(!this.Bd) {
      this.Bd = true;
      this.disposeInternal()
    }
  };
  h.disposeInternal = i();
  b.oa = function(a) {
    a && typeof a.oa == "function" && a.oa()
  };
  b.a = {};
  b.a.k = {};
  b.a.k.sa = function(a, c) {
    a.className = c
  };
  b.a.k.qa = function(a) {
    return(a = a.className) && typeof a.split == "function" ? a.split(" ") : []
  };
  b.a.k.add = function(a) {
    var c = b.a.k.qa(a), d = b.c.slice(arguments, 1);
    d = b.a.k.Ae(c, d);
    a.className = c.join(" ");
    return d
  };
  b.a.k.remove = function(a) {
    var c = b.a.k.qa(a), d = b.c.slice(arguments, 1);
    d = b.a.k.bg(c, d);
    a.className = c.join(" ");
    return d
  };
  b.a.k.Ae = function(a, c) {
    for(var d = 0, e = 0;e < c.length;e++) {
      if(!b.c.contains(a, c[e])) {
        a.push(c[e]);
        d++
      }
    }
    return d == c.length
  };
  b.a.k.bg = function(a, c) {
    for(var d = 0, e = 0;e < a.length;e++) {
      if(b.c.contains(c, a[e])) {
        b.c.splice(a, e--, 1);
        d++
      }
    }
    return d == c.length
  };
  b.a.k.Oj = function(a, c, d) {
    for(var e = b.a.k.qa(a), f = false, g = 0;g < e.length;g++) {
      if(e[g] == c) {
        b.c.splice(e, g--, 1);
        f = true
      }
    }
    if(f) {
      e.push(d);
      a.className = e.join(" ")
    }
    return f
  };
  b.a.k.Eg = function(a, c, d) {
    var e = b.a.k.qa(a);
    if(b.l(c)) {
      b.c.remove(e, c)
    }else {
      b.ia(c) && b.a.k.bg(e, c)
    }
    if(b.l(d) && !b.c.contains(e, d)) {
      e.push(d)
    }else {
      b.ia(d) && b.a.k.Ae(e, d)
    }
    a.className = e.join(" ")
  };
  b.a.k.Nd = function(a, c) {
    return b.c.contains(b.a.k.qa(a), c)
  };
  b.a.k.Ye = function(a, c, d) {
    d ? b.a.k.add(a, c) : b.a.k.remove(a, c)
  };
  b.a.k.Wj = function(a, c) {
    var d = !b.a.k.Nd(a, c);
    b.a.k.Ye(a, c, d);
    return d
  };
  b.Qf = {};
  q.prototype.s = function() {
    return new q(this.x, this.y)
  };
  if(b.Ab) {
    q.prototype.toString = function() {
      return"(" + this.x + ", " + this.y + ")"
    }
  }
  p.prototype.s = function() {
    return new p(this.width, this.height)
  };
  if(b.Ab) {
    p.prototype.toString = function() {
      return"(" + this.width + " x " + this.height + ")"
    }
  }
  h = p.prototype;
  h.Lh = function() {
    return Math.max(this.width, this.height)
  };
  h.ai = function() {
    return Math.min(this.width, this.height)
  };
  h.Ce = function() {
    return this.width * this.height
  };
  h.nd = function() {
    return this.width / this.height
  };
  h.z = function() {
    return!this.Ce()
  };
  h.ceil = function() {
    this.width = Math.ceil(this.width);
    this.height = Math.ceil(this.height);
    return this
  };
  h.ph = function(a) {
    return this.width <= a.width && this.height <= a.height
  };
  h.floor = function() {
    this.width = Math.floor(this.width);
    this.height = Math.floor(this.height);
    return this
  };
  h.round = function() {
    this.width = Math.round(this.width);
    this.height = Math.round(this.height);
    return this
  };
  h.scale = function(a) {
    this.width *= a;
    this.height *= a;
    return this
  };
  h.nj = function(a) {
    return this.scale(this.nd() > a.nd() ? a.width / this.width : a.height / this.height)
  };
  b.object = {};
  b.object.forEach = function(a, c, d) {
    for(var e in a) {
      c.call(d, a[e], e, a)
    }
  };
  b.object.filter = function(a, c, d) {
    var e = {};
    for(var f in a) {
      if(c.call(d, a[f], f, a)) {
        e[f] = a[f]
      }
    }
    return e
  };
  b.object.map = function(a, c, d) {
    var e = {};
    for(var f in a) {
      e[f] = c.call(d, a[f], f, a)
    }
    return e
  };
  b.object.some = function(a, c, d) {
    for(var e in a) {
      if(c.call(d, a[e], e, a)) {
        return true
      }
    }
    return false
  };
  b.object.every = function(a, c, d) {
    for(var e in a) {
      if(!c.call(d, a[e], e, a)) {
        return false
      }
    }
    return true
  };
  b.object.da = function(a) {
    var c = 0;
    for(var d in a) {
      c++
    }
    return c
  };
  b.object.qh = function(a) {
    for(var c in a) {
      return c
    }
  };
  b.object.rh = function(a) {
    for(var c in a) {
      return a[c]
    }
  };
  b.object.contains = function(a, c) {
    return b.object.Wa(a, c)
  };
  b.object.u = function(a) {
    var c = [], d = 0;
    for(var e in a) {
      c[d++] = a[e]
    }
    return c
  };
  b.object.ga = function(a) {
    var c = [], d = 0;
    for(var e in a) {
      c[d++] = e
    }
    return c
  };
  b.object.Eb = function(a, c) {
    return c in a
  };
  b.object.Wa = function(a, c) {
    for(var d in a) {
      if(a[d] == c) {
        return true
      }
    }
    return false
  };
  b.object.cf = function(a, c, d) {
    for(var e in a) {
      if(c.call(d, a[e], e, a)) {
        return e
      }
    }
  };
  b.object.nh = function(a, c, d) {
    return(c = b.object.cf(a, c, d)) && a[c]
  };
  b.object.z = function(a) {
    for(var c in a) {
      return false
    }
    return true
  };
  b.object.clear = function(a) {
    for(var c = b.object.ga(a), d = c.length - 1;d >= 0;d--) {
      b.object.remove(a, c[d])
    }
  };
  b.object.remove = function(a, c) {
    var d;
    if(d = c in a) {
      delete a[c]
    }
    return d
  };
  b.object.add = function(a, c, d) {
    if(c in a) {
      throw Error('The object already contains the key "' + c + '"');
    }
    b.object.sa(a, c, d)
  };
  b.object.qa = function(a, c, d) {
    if(c in a) {
      return a[c]
    }
    return d
  };
  b.object.sa = function(a, c, d) {
    a[c] = d
  };
  b.object.yj = function(a, c, d) {
    return c in a ? a[c] : (a[c] = d)
  };
  b.object.s = function(a) {
    var c = {};
    for(var d in a) {
      c[d] = a[d]
    }
    return c
  };
  b.object.fe = function(a) {
    var c = {};
    for(var d in a) {
      c[a[d]] = d
    }
    return c
  };
  b.object.xe = ["constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable", "toLocaleString", "toString", "valueOf"];
  b.object.extend = function(a) {
    for(var c, d, e = 1;e < arguments.length;e++) {
      d = arguments[e];
      for(c in d) {
        a[c] = d[c]
      }
      for(var f = 0;f < b.object.xe.length;f++) {
        c = b.object.xe[f];
        if(Object.prototype.hasOwnProperty.call(d, c)) {
          a[c] = d[c]
        }
      }
    }
  };
  b.object.create = function() {
    var a = arguments.length;
    if(a == 1 && b.ia(arguments[0])) {
      return b.object.create.apply(null, arguments[0])
    }
    if(a % 2) {
      throw Error("Uneven number of arguments");
    }
    for(var c = {}, d = 0;d < a;d += 2) {
      c[arguments[d]] = arguments[d + 1]
    }
    return c
  };
  b.object.Le = function() {
    var a = arguments.length;
    if(a == 1 && b.ia(arguments[0])) {
      return b.object.Le.apply(null, arguments[0])
    }
    for(var c = {}, d = 0;d < a;d++) {
      c[arguments[d]] = true
    }
    return c
  };
  b.d = {};
  b.d.Lj = function(a, c) {
    return a.indexOf(c) == 0
  };
  b.d.ih = function(a, c) {
    var d = a.length - c.length;
    return d >= 0 && a.lastIndexOf(c, d) == d
  };
  b.d.Og = function(a, c) {
    return b.d.rd(c, a.substr(0, c.length)) == 0
  };
  b.d.Ng = function(a, c) {
    return b.d.rd(c, a.substr(a.length - c.length, c.length)) == 0
  };
  b.d.Nj = function(a) {
    for(var c = 1;c < arguments.length;c++) {
      var d = String(arguments[c]).replace(/\$/g, "$$$$");
      a = a.replace(/\%s/, d)
    }
    return a
  };
  b.d.Rg = function(a) {
    return a.replace(/[\s\xa0]+/g, " ").replace(/^\s+|\s+$/g, "")
  };
  b.d.z = function(a) {
    return/^[\s\xa0]*$/.test(a)
  };
  b.d.Gi = function(a) {
    return b.d.z(b.d.Nf(a))
  };
  b.d.Ai = function(a) {
    return!/[^\t\n\r ]/.test(a)
  };
  b.d.xi = function(a) {
    return!/[^a-zA-Z]/.test(a)
  };
  b.d.Ki = function(a) {
    return!/[^0-9]/.test(a)
  };
  b.d.yi = function(a) {
    return!/[^a-zA-Z0-9]/.test(a)
  };
  b.d.Li = function(a) {
    return a == " "
  };
  b.d.Mi = function(a) {
    return a.length == 1 && a >= " " && a <= "~" || a >= "\u0080" && a <= "\ufffd"
  };
  b.d.Mj = function(a) {
    return a.replace(/(\r\n|\r|\n)+/g, " ")
  };
  b.d.Fe = function(a) {
    return a.replace(/(\r\n|\r|\n)/g, "\n")
  };
  b.d.Xi = function(a) {
    return a.replace(/\xa0|\s/g, " ")
  };
  b.d.Wi = function(a) {
    return a.replace(/\xa0|[ \t]+/g, " ")
  };
  b.d.ge = function(a) {
    return a.replace(/^[\s\xa0]+|[\s\xa0]+$/g, "")
  };
  b.d.lg = function(a) {
    return a.replace(/^[\s\xa0]+/, "")
  };
  b.d.Yj = function(a) {
    return a.replace(/[\s\xa0]+$/, "")
  };
  b.d.rd = function(a, c) {
    a = String(a).toLowerCase();
    c = String(c).toLowerCase();
    return a < c ? -1 : a == c ? 0 : 1
  };
  b.d.Uf = /(\.\d+)|(\d+)|(\D+)/g;
  b.d.Yi = function(a, c) {
    if(a == c) {
      return 0
    }
    if(!a) {
      return-1
    }
    if(!c) {
      return 1
    }
    for(var d = a.toLowerCase().match(b.d.Uf), e = c.toLowerCase().match(b.d.Uf), f = Math.min(d.length, e.length), g = 0;g < f;g++) {
      var j = d[g], l = e[g];
      if(j != l) {
        a = parseInt(j, 10);
        if(!isNaN(a)) {
          c = parseInt(l, 10);
          if(!isNaN(c) && a - c) {
            return a - c
          }
        }
        return j < l ? -1 : 1
      }
    }
    if(d.length != e.length) {
      return d.length - e.length
    }
    return a < c ? -1 : 1
  };
  b.d.hh = /^[a-zA-Z0-9\-_.!~*'()]*$/;
  b.d.hk = function(a) {
    a = String(a);
    if(!b.d.hh.test(a)) {
      return encodeURIComponent(a)
    }
    return a
  };
  b.d.gk = function(a) {
    return decodeURIComponent(a.replace(/\+/g, " "))
  };
  b.d.Tf = function(a, c) {
    return a.replace(/(\r\n|\r|\n)/g, c ? "<br />" : "<br>")
  };
  b.d.Rb = function(a, c) {
    if(c) {
      return a.replace(b.d.Be, "&amp;").replace(b.d.Mf, "&lt;").replace(b.d.Af, "&gt;").replace(b.d.Yf, "&quot;")
    }else {
      if(!b.d.Gg.test(a)) {
        return a
      }
      if(a.indexOf("&") != -1) {
        a = a.replace(b.d.Be, "&amp;")
      }
      if(a.indexOf("<") != -1) {
        a = a.replace(b.d.Mf, "&lt;")
      }
      if(a.indexOf(">") != -1) {
        a = a.replace(b.d.Af, "&gt;")
      }
      if(a.indexOf('"') != -1) {
        a = a.replace(b.d.Yf, "&quot;")
      }
      return a
    }
  };
  b.d.Be = /&/g;
  b.d.Mf = /</g;
  b.d.Af = />/g;
  b.d.Yf = /\"/g;
  b.d.Gg = /[&<>\"]/;
  b.d.ie = function(a) {
    if(b.d.contains(a, "&")) {
      return"document" in b.global && !b.d.contains(a, "<") ? b.d.ak(a) : b.d.bk(a)
    }
    return a
  };
  b.d.ak = function(a) {
    var c = b.global.document.createElement("a");
    c.innerHTML = a;
    c[b.d.we] && c[b.d.we]();
    a = c.firstChild.nodeValue;
    c.innerHTML = "";
    return a
  };
  b.d.bk = function(a) {
    return a.replace(/&([^;]+);/g, function(c, d) {
      switch(d) {
        case "amp":
          return"&";
        case "lt":
          return"<";
        case "gt":
          return">";
        case "quot":
          return'"';
        default:
          if(d.charAt(0) == "#") {
            d = Number("0" + d.substr(1));
            if(!isNaN(d)) {
              return String.fromCharCode(d)
            }
          }
          return c
      }
    })
  };
  b.d.we = "normalize";
  b.d.jk = function(a, c) {
    return b.d.Tf(a.replace(/  /g, " &#160;"), c)
  };
  b.d.ig = function(a, c) {
    for(var d = c.length, e = 0;e < d;e++) {
      var f = d == 1 ? c : c.charAt(e);
      if(a.charAt(0) == f && a.charAt(a.length - 1) == f) {
        return a.substring(1, a.length - 1)
      }
    }
    return a
  };
  b.d.Zj = function(a, c, d) {
    if(d) {
      a = b.d.ie(a)
    }
    if(a.length > c) {
      a = a.substring(0, c - 3) + "..."
    }
    if(d) {
      a = b.d.Rb(a)
    }
    return a
  };
  b.d.$j = function(a, c, d) {
    if(d) {
      a = b.d.ie(a)
    }
    if(a.length > c) {
      var e = Math.floor(c / 2), f = a.length - e;
      e += c % 2;
      a = a.substring(0, e) + "..." + a.substring(f)
    }
    if(d) {
      a = b.d.Rb(a)
    }
    return a
  };
  b.d.Ud = {"\u0008":"\\b", "\u000c":"\\f", "\n":"\\n", "\r":"\\r", "\t":"\\t", "\u000b":"\\x0B", '"':'\\"', "'":"\\'", "\\":"\\\\"};
  b.d.quote = function(a) {
    a = String(a);
    if(a.quote) {
      return a.quote()
    }else {
      for(var c = ['"'], d = 0;d < a.length;d++) {
        c[d + 1] = b.d.Ze(a.charAt(d))
      }
      c.push('"');
      return c.join("")
    }
  };
  b.d.Ze = function(a) {
    if(a in b.d.Ud) {
      return b.d.Ud[a]
    }
    var c = a, d = a.charCodeAt(0);
    if(d > 31 && d < 127) {
      c = a
    }else {
      if(d < 256) {
        c = "\\x";
        if(d < 16 || d > 256) {
          c += "0"
        }
      }else {
        c = "\\u";
        if(d < 4096) {
          c += "0"
        }
      }
      c += d.toString(16).toUpperCase()
    }
    return b.d.Ud[a] = c
  };
  b.d.Tj = function(a) {
    for(var c = {}, d = 0;d < a.length;d++) {
      c[a.charAt(d)] = true
    }
    return c
  };
  b.d.contains = function(a, c) {
    return a.indexOf(c) != -1
  };
  b.d.ub = function(a, c, d) {
    var e = a;
    if(c >= 0 && c < a.length && d > 0) {
      e = a.substr(0, c) + a.substr(c + d, a.length - c - d)
    }
    return e
  };
  b.d.remove = function(a, c) {
    c = new RegExp(b.d.ce(c), "");
    return a.replace(c, "")
  };
  b.d.Ia = function(a, c) {
    c = new RegExp(b.d.ce(c), "g");
    return a.replace(c, "")
  };
  b.d.ce = function(a) {
    return String(a).replace(/([-()\[\]{}+?*.$\^|,:#<!\\])/g, "\\$1").replace(/\x08/g, "\\x08")
  };
  b.d.repeat = function(a, c) {
    return(new Array(c + 1)).join(a)
  };
  b.d.aj = function(a, c, d) {
    a = b.Oa(d) ? a.toFixed(d) : String(a);
    d = a.indexOf(".");
    if(d == -1) {
      d = a.length
    }
    return b.d.repeat("0", Math.max(0, c - d)) + a
  };
  b.d.Nf = function(a) {
    return a == null ? "" : String(a)
  };
  b.d.Mg = function() {
    return Array.prototype.join.call(arguments, "")
  };
  b.d.Vh = function() {
    return Math.floor(Math.random() * 2147483648).toString(36) + (Math.floor(Math.random() * 2147483648) ^ (new Date).getTime()).toString(36)
  };
  b.d.hc = function(a, c) {
    var d = 0;
    a = b.d.ge(String(a)).split(".");
    c = b.d.ge(String(c)).split(".");
    for(var e = Math.max(a.length, c.length), f = 0;d == 0 && f < e;f++) {
      var g = a[f] || "", j = c[f] || "", l = new RegExp("(\\d*)(\\D*)", "g"), r = new RegExp("(\\d*)(\\D*)", "g");
      do {
        var D = l.exec(g) || ["", "", ""], y = r.exec(j) || ["", "", ""];
        if(D[0].length == 0 && y[0].length == 0) {
          break
        }
        d = D[1].length == 0 ? 0 : parseInt(D[1], 10);
        var R = y[1].length == 0 ? 0 : parseInt(y[1], 10);
        d = b.d.ud(d, R) || b.d.ud(D[2].length == 0, y[2].length == 0) || b.d.ud(D[2], y[2])
      }while(d == 0)
    }
    return d
  };
  b.d.ud = function(a, c) {
    if(a < c) {
      return-1
    }else {
      if(a > c) {
        return 1
      }
    }
    return 0
  };
  b.d.wg = 4294967296;
  b.d.mi = function(a) {
    for(var c = 0, d = 0;d < a.length;++d) {
      c = 31 * c + a.charCodeAt(d);
      c %= b.d.wg
    }
    return c
  };
  b.d.ek = b.now();
  b.d.Vg = function() {
    return"goog_" + b.d.ek++
  };
  b.d.Uj = function(a) {
    var c = Number(a);
    if(c == 0 && b.d.z(a)) {
      return NaN
    }
    return c
  };
  b.userAgent = {};
  b.userAgent.$b = false;
  b.userAgent.cd = false;
  b.userAgent.jd = false;
  b.userAgent.ac = false;
  b.userAgent.cc = false;
  b.userAgent.hb = b.userAgent.$b || b.userAgent.cd || b.userAgent.ac || b.userAgent.jd || b.userAgent.cc;
  b.userAgent.Gc = function() {
    return b.global.navigator ? b.global.navigator.userAgent : null
  };
  b.userAgent.Ob = function() {
    return b.global.navigator
  };
  b.userAgent.Sb = function() {
    b.userAgent.Gb = false;
    b.userAgent.Te = false;
    b.userAgent.ic = false;
    b.userAgent.Ve = false;
    b.userAgent.Se = false;
    var a;
    if(!b.userAgent.hb && (a = b.userAgent.Gc())) {
      var c = b.userAgent.Ob();
      b.userAgent.Gb = a.indexOf("Opera") == 0;
      b.userAgent.Te = !b.userAgent.Gb && a.indexOf("MSIE") != -1;
      b.userAgent.ic = !b.userAgent.Gb && a.indexOf("WebKit") != -1;
      b.userAgent.Ve = b.userAgent.ic && a.indexOf("Mobile") != -1;
      b.userAgent.Se = !b.userAgent.Gb && !b.userAgent.ic && c.product == "Gecko"
    }
  };
  b.userAgent.hb || b.userAgent.Sb();
  b.userAgent.fa = b.userAgent.hb ? b.userAgent.cc : b.userAgent.Gb;
  b.userAgent.j = b.userAgent.hb ? b.userAgent.$b : b.userAgent.Te;
  b.userAgent.la = b.userAgent.hb ? b.userAgent.cd : b.userAgent.Se;
  b.userAgent.ca = b.userAgent.hb ? b.userAgent.jd || b.userAgent.ac : b.userAgent.ic;
  b.userAgent.ve = b.userAgent.ac || b.userAgent.Ve;
  b.userAgent.md = b.userAgent.ca;
  b.userAgent.dh = function() {
    var a = b.userAgent.Ob();
    return a && a.platform || ""
  };
  b.userAgent.ec = b.userAgent.dh();
  b.userAgent.fd = false;
  b.userAgent.kd = false;
  b.userAgent.ed = false;
  b.userAgent.ld = false;
  b.userAgent.Bb = b.userAgent.fd || b.userAgent.kd || b.userAgent.ed || b.userAgent.ld;
  b.userAgent.qi = function() {
    b.userAgent.$g = b.d.contains(b.userAgent.ec, "Mac");
    b.userAgent.ah = b.d.contains(b.userAgent.ec, "Win");
    b.userAgent.Zg = b.d.contains(b.userAgent.ec, "Linux");
    b.userAgent.bh = !!b.userAgent.Ob() && b.d.contains(b.userAgent.Ob().appVersion || "", "X11")
  };
  b.userAgent.Bb || b.userAgent.qi();
  b.userAgent.dc = b.userAgent.Bb ? b.userAgent.fd : b.userAgent.$g;
  b.userAgent.Bg = b.userAgent.Bb ? b.userAgent.kd : b.userAgent.ah;
  b.userAgent.se = b.userAgent.Bb ? b.userAgent.ed : b.userAgent.Zg;
  b.userAgent.ze = b.userAgent.Bb ? b.userAgent.ld : b.userAgent.bh;
  b.userAgent.eh = function() {
    var a = "", c;
    if(b.userAgent.fa && b.global.opera) {
      a = b.global.opera.version;
      a = typeof a == "function" ? a() : a
    }else {
      if(b.userAgent.la) {
        c = /rv\:([^\);]+)(\)|;)/
      }else {
        if(b.userAgent.j) {
          c = /MSIE\s+([^\);]+)(\)|;)/
        }else {
          if(b.userAgent.ca) {
            c = /WebKit\/(\S+)/
          }
        }
      }
      if(c) {
        a = (a = c.exec(b.userAgent.Gc())) ? a[1] : ""
      }
    }
    return a
  };
  b.userAgent.Cb = b.userAgent.eh();
  b.userAgent.td = function(a, c) {
    return b.d.hc(a, c)
  };
  b.userAgent.Kf = {};
  b.userAgent.aa = function(a) {
    return b.userAgent.Kf[a] || (b.userAgent.Kf[a] = b.d.hc(b.userAgent.Cb, a) >= 0)
  };
  b.a.le = false;
  b.a.hd = false;
  b.a.rg = b.a.le || b.a.hd;
  b.a.ma = function(a) {
    return a ? new n(b.a.t(a)) : b.a.Wg || (b.a.Wg = new n)
  };
  b.a.Na = function() {
    return document
  };
  b.a.Lb = function(a) {
    return b.l(a) ? document.getElementById(a) : a
  };
  b.a.Wc = b.a.Lb;
  b.a.Za = function(a, c, d) {
    return b.a.pf(document, a, c, d)
  };
  b.a.pf = function(a, c, d, e) {
    e = e || a;
    c = c && c != "*" ? c.toLowerCase() : "";
    if(e.querySelectorAll && (c || d) && (!b.userAgent.ca || b.a.ob(a) || b.userAgent.aa("528"))) {
      return e.querySelectorAll(c + (d ? "." + d : ""))
    }
    if(d && e.getElementsByClassName) {
      a = e.getElementsByClassName(d);
      if(c) {
        e = {};
        for(var f = 0, g = 0, j;j = a[g];g++) {
          if(c == j.nodeName.toLowerCase()) {
            e[f++] = j
          }
        }
        e.length = f;
        return e
      }else {
        return a
      }
    }
    a = e.getElementsByTagName(c || "*");
    if(d) {
      e = {};
      for(g = f = 0;j = a[g];g++) {
        c = j.className;
        if(typeof c.split == "function" && b.c.contains(c.split(" "), d)) {
          e[f++] = j
        }
      }
      e.length = f;
      return e
    }else {
      return a
    }
  };
  b.a.Xc = b.a.Za;
  b.a.Wb = function(a, c) {
    b.object.forEach(c, function(d, e) {
      if(e == "style") {
        a.style.cssText = d
      }else {
        if(e == "class") {
          a.className = d
        }else {
          if(e == "for") {
            a.htmlFor = d
          }else {
            if(e in b.a.qe) {
              a.setAttribute(b.a.qe[e], d)
            }else {
              a[e] = d
            }
          }
        }
      }
    })
  };
  b.a.qe = {cellpadding:"cellPadding", cellspacing:"cellSpacing", colspan:"colSpan", rowspan:"rowSpan", valign:"vAlign", height:"height", width:"width", usemap:"useMap", frameborder:"frameBorder", type:"type"};
  b.a.Pb = function(a) {
    return b.a.zf(a || window)
  };
  b.a.zf = function(a) {
    var c = a.document;
    if(b.userAgent.ca && !b.userAgent.aa("500") && !b.userAgent.ve) {
      if(typeof a.innerHeight == "undefined") {
        a = window
      }
      c = a.innerHeight;
      var d = a.document.documentElement.scrollHeight;
      if(a == a.top) {
        if(d < c) {
          c -= 15
        }
      }
      return new p(a.innerWidth, c)
    }
    a = b.a.ob(c) && (!b.userAgent.fa || b.userAgent.fa && b.userAgent.aa("9.50")) ? c.documentElement : c.body;
    return new p(a.clientWidth, a.clientHeight)
  };
  b.a.Gd = function() {
    return b.a.nf(window)
  };
  b.a.nf = function(a) {
    var c = a.document, d = 0;
    if(c) {
      a = b.a.zf(a).height;
      d = c.body;
      var e = c.documentElement;
      if(b.a.ob(c) && e.scrollHeight) {
        d = e.scrollHeight != a ? e.scrollHeight : e.offsetHeight
      }else {
        c = e.scrollHeight;
        var f = e.offsetHeight;
        if(e.clientHeight != f) {
          c = d.scrollHeight;
          f = d.offsetHeight
        }
        d = c > a ? c > f ? c : f : c < f ? c : f
      }
    }
    return d
  };
  b.a.Th = function(a) {
    return b.a.ma((a || b.global || window).document).Ya()
  };
  b.a.Ya = function() {
    return b.a.of(document)
  };
  b.a.of = function(a) {
    a = b.a.Hd(a);
    return new q(a.scrollLeft, a.scrollTop)
  };
  b.a.sc = function() {
    return b.a.Hd(document)
  };
  b.a.Hd = function(a) {
    return!b.userAgent.ca && b.a.ob(a) ? a.documentElement : a.body
  };
  b.a.$a = function(a) {
    return a ? b.a.Md(a) : window
  };
  b.a.Md = function(a) {
    return a.parentWindow || a.defaultView
  };
  b.a.La = function() {
    return b.a.Je(document, arguments)
  };
  b.a.Je = function(a, c) {
    var d = c[0], e = c[1];
    if(b.userAgent.j && e && (e.name || e.type)) {
      d = ["<", d];
      e.name && d.push(' name="', b.d.Rb(e.name), '"');
      if(e.type) {
        d.push(' type="', b.d.Rb(e.type), '"');
        e = b.sd(e);
        delete e.type
      }
      d.push(">");
      d = d.join("")
    }
    var f = a.createElement(d);
    if(e) {
      if(b.l(e)) {
        f.className = e
      }else {
        b.a.Wb(f, e)
      }
    }
    if(c.length > 2) {
      e = function(j) {
        if(j) {
          f.appendChild(b.l(j) ? a.createTextNode(j) : j)
        }
      };
      for(d = 2;d < c.length;d++) {
        var g = c[d];
        b.ba(g) && !b.a.Tb(g) ? b.c.forEach(b.a.Ff(g) ? b.c.s(g) : g, e) : e(g)
      }
    }
    return f
  };
  b.a.Yc = b.a.La;
  b.a.createElement = function(a) {
    return document.createElement(a)
  };
  b.a.createTextNode = function(a) {
    return document.createTextNode(a)
  };
  b.a.Od = function(a) {
    return b.a.Bf(document, a)
  };
  b.a.Bf = function(a, c) {
    var d = a.createElement("div");
    d.innerHTML = c;
    if(d.childNodes.length == 1) {
      return d.firstChild
    }else {
      for(a = a.createDocumentFragment();d.firstChild;) {
        a.appendChild(d.firstChild)
      }
      return a
    }
  };
  b.a.Fd = function() {
    return b.a.Ca() ? "CSS1Compat" : "BackCompat"
  };
  b.a.Ca = function() {
    return b.a.ob(document)
  };
  b.a.ob = function(a) {
    if(b.a.rg) {
      return b.a.hd
    }
    return a.compatMode == "CSS1Compat"
  };
  b.a.canHaveChildren = function(a) {
    if(a.nodeType != 1) {
      return false
    }
    if("canHaveChildren" in a) {
      return a.canHaveChildren
    }
    switch(a.tagName) {
      case "APPLET":
      ;
      case "AREA":
      ;
      case "BASE":
      ;
      case "BR":
      ;
      case "COL":
      ;
      case "FRAME":
      ;
      case "HR":
      ;
      case "IMG":
      ;
      case "INPUT":
      ;
      case "IFRAME":
      ;
      case "ISINDEX":
      ;
      case "LINK":
      ;
      case "NOFRAMES":
      ;
      case "NOSCRIPT":
      ;
      case "META":
      ;
      case "OBJECT":
      ;
      case "PARAM":
      ;
      case "SCRIPT":
      ;
      case "STYLE":
        return false
    }
    return true
  };
  b.a.appendChild = function(a, c) {
    a.appendChild(c)
  };
  b.a.Vb = function(a) {
    for(var c;c = a.firstChild;) {
      a.removeChild(c)
    }
  };
  b.a.Lc = function(a, c) {
    c.parentNode && c.parentNode.insertBefore(a, c)
  };
  b.a.Kc = function(a, c) {
    c.parentNode && c.parentNode.insertBefore(a, c.nextSibling)
  };
  b.a.removeNode = function(a) {
    return a && a.parentNode ? a.parentNode.removeChild(a) : null
  };
  b.a.Qc = function(a, c) {
    var d = c.parentNode;
    d && d.replaceChild(a, c)
  };
  b.a.nc = function(a) {
    var c, d = a.parentNode;
    if(d && d.nodeType != 11) {
      if(a.removeNode) {
        return a.removeNode(false)
      }else {
        for(;c = a.firstChild;) {
          d.insertBefore(c, a)
        }
        return b.a.removeNode(a)
      }
    }
  };
  b.a.tc = function(a) {
    return b.a.yc(a.firstChild, true)
  };
  b.a.xc = function(a) {
    return b.a.yc(a.lastChild, false)
  };
  b.a.zc = function(a) {
    return b.a.yc(a.nextSibling, true)
  };
  b.a.Dc = function(a) {
    return b.a.yc(a.previousSibling, false)
  };
  b.a.yc = function(a, c) {
    for(;a && a.nodeType != 1;) {
      a = c ? a.nextSibling : a.previousSibling
    }
    return a
  };
  b.a.Tb = function(a) {
    return b.Pa(a) && a.nodeType > 0
  };
  b.a.contains = function(a, c) {
    if(a.contains && c.nodeType == 1) {
      return a == c || a.contains(c)
    }
    if(typeof a.compareDocumentPosition != "undefined") {
      return a == c || Boolean(a.compareDocumentPosition(c) & 16)
    }
    for(;c && a != c;) {
      c = c.parentNode
    }
    return c == a
  };
  b.a.Sg = function(a, c) {
    if(a == c) {
      return 0
    }
    if(a.compareDocumentPosition) {
      return a.compareDocumentPosition(c) & 2 ? 1 : -1
    }
    if("sourceIndex" in a || a.parentNode && "sourceIndex" in a.parentNode) {
      var d = a.nodeType == 1, e = c.nodeType == 1;
      if(d && e) {
        return a.sourceIndex - c.sourceIndex
      }else {
        var f = a.parentNode, g = c.parentNode;
        if(f == g) {
          return b.a.Ie(a, c)
        }
        if(!d && b.a.contains(f, c)) {
          return-1 * b.a.He(a, c)
        }
        if(!e && b.a.contains(g, a)) {
          return b.a.He(c, a)
        }
        return(d ? a.sourceIndex : f.sourceIndex) - (e ? c.sourceIndex : g.sourceIndex)
      }
    }
    e = b.a.t(a);
    d = e.createRange();
    d.selectNode(a);
    d.collapse(true);
    a = e.createRange();
    a.selectNode(c);
    a.collapse(true);
    return d.compareBoundaryPoints(b.global.Range.START_TO_END, a)
  };
  b.a.He = function(a, c) {
    var d = a.parentNode;
    if(d == c) {
      return-1
    }
    for(c = c;c.parentNode != d;) {
      c = c.parentNode
    }
    return b.a.Ie(c, a)
  };
  b.a.Ie = function(a, c) {
    for(c = c;c = c.previousSibling;) {
      if(c == a) {
        return-1
      }
    }
    return 1
  };
  b.a.lh = function() {
    var a, c = arguments.length;
    if(c) {
      if(c == 1) {
        return arguments[0]
      }
    }else {
      return null
    }
    var d = [], e = Infinity;
    for(a = 0;a < c;a++) {
      for(var f = [], g = arguments[a];g;) {
        f.unshift(g);
        g = g.parentNode
      }
      d.push(f);
      e = Math.min(e, f.length)
    }
    f = null;
    for(a = 0;a < e;a++) {
      g = d[0][a];
      for(var j = 1;j < c;j++) {
        if(g != d[j][a]) {
          return f
        }
      }
      f = g
    }
    return f
  };
  b.a.t = function(a) {
    return a.nodeType == 9 ? a : a.ownerDocument || a.document
  };
  b.a.Mb = function(a) {
    return b.userAgent.ca ? a.document || a.contentWindow.document : a.contentDocument || a.contentWindow.document
  };
  b.a.uc = function(a) {
    return a.contentWindow || b.a.Md(b.a.Mb(a))
  };
  b.a.Rc = function(a, c) {
    if("textContent" in a) {
      a.textContent = c
    }else {
      if(a.firstChild && a.firstChild.nodeType == 3) {
        for(;a.lastChild != a.firstChild;) {
          a.removeChild(a.lastChild)
        }
        a.firstChild.data = c
      }else {
        b.a.Vb(a);
        var d = b.a.t(a);
        a.appendChild(d.createTextNode(c))
      }
    }
  };
  b.a.Qh = function(a) {
    if("outerHTML" in a) {
      return a.outerHTML
    }else {
      var c = b.a.t(a).createElement("div");
      c.appendChild(a.cloneNode(true));
      return c.innerHTML
    }
  };
  b.a.kc = function(a, c) {
    var d = [];
    return b.a.Ed(a, c, d, true) ? d[0] : undefined
  };
  b.a.lc = function(a, c) {
    var d = [];
    b.a.Ed(a, c, d, false);
    return d
  };
  b.a.Ed = function(a, c, d, e) {
    if(a != null) {
      for(var f = 0, g;g = a.childNodes[f];f++) {
        if(c(g)) {
          d.push(g);
          if(e) {
            return true
          }
        }
        if(b.a.Ed(g, c, d, e)) {
          return true
        }
      }
    }
    return false
  };
  b.a.ye = {SCRIPT:1, STYLE:1, HEAD:1, IFRAME:1, OBJECT:1};
  b.a.fc = {IMG:" ", BR:"\n"};
  b.a.Hi = function(a) {
    var c = a.getAttributeNode("tabindex");
    if(c && c.specified) {
      a = a.tabIndex;
      return b.Gf(a) && a >= 0
    }
    return false
  };
  b.a.xj = function(a, c) {
    if(c) {
      a.tabIndex = 0
    }else {
      a.removeAttribute("tabIndex")
    }
  };
  b.a.mb = function(a) {
    if(b.userAgent.j && "innerText" in a) {
      a = b.d.Fe(a.innerText)
    }else {
      var c = [];
      b.a.Ld(a, c, true);
      a = c.join("")
    }
    a = a.replace(/\xAD/g, "");
    a = a.replace(/ +/g, " ");
    if(a != " ") {
      a = a.replace(/^\s*/, "")
    }
    return a
  };
  b.a.Wh = function(a) {
    var c = [];
    b.a.Ld(a, c, false);
    return c.join("")
  };
  b.a.Ld = function(a, c, d) {
    if(!(a.nodeName in b.a.ye)) {
      if(a.nodeType == 3) {
        d ? c.push(String(a.nodeValue).replace(/(\r\n|\r|\n)/g, "")) : c.push(a.nodeValue)
      }else {
        if(a.nodeName in b.a.fc) {
          c.push(b.a.fc[a.nodeName])
        }else {
          for(a = a.firstChild;a;) {
            b.a.Ld(a, c, d);
            a = a.nextSibling
          }
        }
      }
    }
  };
  b.a.Ac = function(a) {
    return b.a.mb(a).length
  };
  b.a.Bc = function(a, c) {
    c = c || b.a.t(a).body;
    for(var d = [];a && a != c;) {
      for(var e = a;e = e.previousSibling;) {
        d.unshift(b.a.mb(e))
      }
      a = a.parentNode
    }
    return b.d.lg(d.join("")).replace(/ +/g, " ").length
  };
  b.a.Oh = function(a, c, d) {
    a = [a];
    for(var e = 0, f;a.length > 0 && e < c;) {
      f = a.pop();
      if(!(f.nodeName in b.a.ye)) {
        if(f.nodeType == 3) {
          var g = f.nodeValue.replace(/(\r\n|\r|\n)/g, "").replace(/ +/g, " ");
          e += g.length
        }else {
          if(f.nodeName in b.a.fc) {
            e += b.a.fc[f.nodeName].length
          }else {
            for(g = f.childNodes.length - 1;g >= 0;g--) {
              a.push(f.childNodes[g])
            }
          }
        }
      }
    }
    if(b.Pa(d)) {
      d.ym = f ? f.nodeValue.length + c - e - 1 : 0;
      d.wm = f
    }
    return f
  };
  b.a.Ff = function(a) {
    if(a && typeof a.length == "number") {
      if(b.Pa(a)) {
        return typeof a.item == "function" || typeof a.item == "string"
      }else {
        if(b.pb(a)) {
          return typeof a.item == "function"
        }
      }
    }
    return false
  };
  b.a.oc = function(a, c, d) {
    return b.a.Ib(a, function(e) {
      return(!c || e.nodeName == c) && (!d || b.a.k.Nd(e, d))
    }, true)
  };
  b.a.Ib = function(a, c, d, e) {
    if(!d) {
      a = a.parentNode
    }
    for(d = 0;a && (e == null || d <= e);) {
      if(c(a)) {
        return a
      }
      a = a.parentNode;
      d++
    }
    return null
  };
  h = n.prototype;
  h.ma = b.a.ma;
  h.uj = k("pa");
  h.Na = m("pa");
  h.Lb = function(a) {
    return b.l(a) ? this.pa.getElementById(a) : a
  };
  h.Wc = n.prototype.Lb;
  h.Za = function(a, c, d) {
    return b.a.pf(this.pa, a, c, d)
  };
  h.Xc = n.prototype.Za;
  h.Wb = b.a.Wb;
  h.Pb = function(a) {
    return b.a.Pb(a || this.$a())
  };
  h.Gd = function() {
    return b.a.nf(this.$a())
  };
  h.La = function() {
    return b.a.Je(this.pa, arguments)
  };
  h.Yc = n.prototype.La;
  h.createElement = function(a) {
    return this.pa.createElement(a)
  };
  h.createTextNode = function(a) {
    return this.pa.createTextNode(a)
  };
  h.Od = function(a) {
    return b.a.Bf(this.pa, a)
  };
  h.Fd = function() {
    return this.Ca() ? "CSS1Compat" : "BackCompat"
  };
  h.Ca = function() {
    return b.a.ob(this.pa)
  };
  h.$a = function() {
    return b.a.Md(this.pa)
  };
  h.sc = function() {
    return b.a.Hd(this.pa)
  };
  h.Ya = function() {
    return b.a.of(this.pa)
  };
  h.appendChild = b.a.appendChild;
  h.Vb = b.a.Vb;
  h.Lc = b.a.Lc;
  h.Kc = b.a.Kc;
  h.removeNode = b.a.removeNode;
  h.Qc = b.a.Qc;
  h.nc = b.a.nc;
  h.tc = b.a.tc;
  h.xc = b.a.xc;
  h.zc = b.a.zc;
  h.Dc = b.a.Dc;
  h.Tb = b.a.Tb;
  h.contains = b.a.contains;
  h.t = b.a.t;
  h.Mb = b.a.Mb;
  h.uc = b.a.uc;
  h.Rc = b.a.Rc;
  h.kc = b.a.kc;
  h.lc = b.a.lc;
  h.mb = b.a.mb;
  h.Ac = b.a.Ac;
  h.Bc = b.a.Bc;
  h.oc = b.a.oc;
  h.Ib = b.a.Ib;
  b.a.xml = {};
  b.a.xml.ue = 2048;
  b.a.xml.te = 256;
  b.a.xml.createDocument = function(a, c) {
    if(c && !a) {
      throw Error("Can't create document with namespace and no root tag");
    }
    if(document.implementation && document.implementation.createDocument) {
      return document.implementation.createDocument(c || "", a || "", null)
    }else {
      if(typeof ActiveXObject != "undefined") {
        var d = b.a.xml.Ke();
        if(d) {
          if(a) {
            d.appendChild(d.createNode(1, a, c || ""))
          }
          return d
        }
      }
    }
    throw Error("Your browser does not support creating new documents");
  };
  b.a.xml.Ri = function(a) {
    if(typeof DOMParser != "undefined") {
      return(new DOMParser).parseFromString(a, "application/xml")
    }else {
      if(typeof ActiveXObject != "undefined") {
        var c = b.a.xml.Ke();
        c.loadXML(a);
        return c
      }
    }
    throw Error("Your browser does not support loading xml documents");
  };
  b.a.xml.wb = function(a) {
    if(typeof XMLSerializer != "undefined") {
      return(new XMLSerializer).serializeToString(a)
    }
    if(a = a.xml) {
      return a
    }
    throw Error("Your browser does not support serializing XML documents");
  };
  b.a.xml.selectSingleNode = function(a, c) {
    if(typeof a.selectSingleNode != "undefined") {
      var d = b.a.t(a);
      typeof d.setProperty != "undefined" && d.setProperty("SelectionLanguage", "XPath");
      return a.selectSingleNode(c)
    }else {
      if(document.implementation.hasFeature("XPath", "3.0")) {
        d = b.a.t(a);
        var e = d.createNSResolver(d.documentElement);
        return d.evaluate(c, a, e, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue
      }
    }
    return null
  };
  b.a.xml.selectNodes = function(a, c) {
    if(typeof a.selectNodes != "undefined") {
      var d = b.a.t(a);
      typeof d.setProperty != "undefined" && d.setProperty("SelectionLanguage", "XPath");
      return a.selectNodes(c)
    }else {
      if(document.implementation.hasFeature("XPath", "3.0")) {
        d = b.a.t(a);
        var e = d.createNSResolver(d.documentElement);
        a = d.evaluate(c, a, e, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
        c = [];
        d = a.snapshotLength;
        for(e = 0;e < d;e++) {
          c.push(a.snapshotItem(e))
        }
        return c
      }else {
        return[]
      }
    }
  };
  b.a.xml.Ke = function() {
    var a = new ActiveXObject("MSXML2.DOMDocument");
    if(a) {
      a.resolveExternals = false;
      a.validateOnParse = false;
      a.setProperty("ProhibitDTD", true);
      a.setProperty("MaxXMLSize", b.a.xml.ue);
      a.setProperty("MaxElementDepth", b.a.xml.te)
    }
    return a
  };
  b.b = {};
  b.Ba(H, o);
  h = H.prototype;
  h.disposeInternal = function() {
    delete this.type;
    delete this.target;
    delete this.currentTarget
  };
  h.Sa = false;
  h.vb = true;
  h.stopPropagation = function() {
    this.Sa = true
  };
  h.preventDefault = function() {
    this.vb = false
  };
  b.Ba(s, H);
  var wa = [1, 4, 2];
  h = s.prototype;
  h.target = null;
  h.relatedTarget = null;
  h.offsetX = 0;
  h.offsetY = 0;
  h.clientX = 0;
  h.clientY = 0;
  h.screenX = 0;
  h.screenY = 0;
  h.button = 0;
  h.keyCode = 0;
  h.charCode = 0;
  h.ctrlKey = false;
  h.altKey = false;
  h.shiftKey = false;
  h.metaKey = false;
  h.Ha = null;
  h.bb = function(a, c) {
    var d = this.type = a.type;
    this.target = a.target || a.srcElement;
    this.currentTarget = c;
    if(c = a.relatedTarget) {
      if(b.userAgent.la) {
        try {
          c = c.nodeName && c
        }catch(e) {
          c = null
        }
      }
    }else {
      if(d == "mouseover") {
        c = a.fromElement
      }else {
        if(d == "mouseout") {
          c = a.toElement
        }
      }
    }
    this.relatedTarget = c;
    this.offsetX = a.offsetX !== undefined ? a.offsetX : a.layerX;
    this.offsetY = a.offsetY !== undefined ? a.offsetY : a.layerY;
    this.clientX = a.clientX !== undefined ? a.clientX : a.pageX;
    this.clientY = a.clientY !== undefined ? a.clientY : a.pageY;
    this.screenX = a.screenX || 0;
    this.screenY = a.screenY || 0;
    this.button = a.button;
    this.keyCode = a.keyCode || 0;
    this.charCode = a.charCode || (d == "keypress" ? a.keyCode : 0);
    this.ctrlKey = a.ctrlKey;
    this.altKey = a.altKey;
    this.shiftKey = a.shiftKey;
    this.metaKey = a.metaKey;
    this.Ha = a;
    delete this.vb;
    delete this.Sa
  };
  h.Bi = function(a) {
    return b.userAgent.j ? this.type == "click" ? a == 0 : !!(this.Ha.button & wa[a]) : this.Ha.button == a
  };
  h.stopPropagation = function() {
    this.Sa = true;
    if(this.Ha.stopPropagation) {
      this.Ha.stopPropagation()
    }else {
      this.Ha.cancelBubble = true
    }
  };
  var xa = b.userAgent.j && !b.userAgent.aa("8");
  s.prototype.preventDefault = function() {
    this.vb = false;
    var a = this.Ha;
    if(a.preventDefault) {
      a.preventDefault()
    }else {
      a.returnValue = false;
      if(xa) {
        try {
          if(a.ctrlKey || a.keyCode >= 112 && a.keyCode <= 123) {
            a.keyCode = -1
          }
        }catch(c) {
        }
      }
    }
  };
  s.prototype.jf = m("Ha");
  s.prototype.disposeInternal = function() {
    s.superClass_.disposeInternal.call(this);
    this.relatedTarget = this.currentTarget = this.target = this.Ha = null
  };
  b.b.Ta = i();
  b.b.Ta.prototype.ea = i();
  b.b.Ta.prototype.Ea = i();
  var ya = 0;
  h = L.prototype;
  h.wa = 0;
  h.eb = false;
  h.gc = false;
  h.bb = function(a, c, d, e, f, g) {
    if(b.pb(a)) {
      this.Ef = true
    }else {
      if(a && a.handleEvent && b.pb(a.handleEvent)) {
        this.Ef = false
      }else {
        throw Error("Invalid listener argument");
      }
    }
    this.tb = a;
    this.Xf = c;
    this.src = d;
    this.type = e;
    this.capture = !!f;
    this.Ic = g;
    this.gc = false;
    this.wa = ++ya;
    this.eb = false
  };
  h.handleEvent = function(a) {
    if(this.Ef) {
      return this.tb.call(this.Ic || this.src, a)
    }
    return this.tb.handleEvent.call(this.tb, a)
  };
  b.g = {};
  b.Ba(B, o);
  h = B.prototype;
  h.xd = null;
  h.Ad = null;
  h.xb = k("xd");
  h.tj = k("Ad");
  h.ta = function() {
    if(this.Xa.length) {
      return this.Xa.pop()
    }
    return this.wd()
  };
  h.xa = function(a) {
    this.Xa.length < this.Rf ? this.Xa.push(a) : this.zd(a)
  };
  h.wd = function() {
    return this.xd ? this.xd() : {}
  };
  h.zd = function(a) {
    if(this.Ad) {
      this.Ad(a)
    }else {
      if(b.pb(a.oa)) {
        a.oa()
      }else {
        for(var c in a) {
          delete a[c]
        }
      }
    }
  };
  h.disposeInternal = function() {
    B.superClass_.disposeInternal.call(this);
    for(var a = this.Xa;a.length;) {
      this.zd(a.pop())
    }
    delete this.Xa
  };
  b.userAgent.q = {};
  b.userAgent.q.bc = false;
  b.userAgent.q.Sb = function() {
    b.userAgent.q.pe = "ScriptEngine" in b.global && b.global.ScriptEngine() == "JScript";
    b.userAgent.q.tg = b.userAgent.q.pe ? b.global.ScriptEngineMajorVersion() + "." + b.global.ScriptEngineMinorVersion() + "." + b.global.ScriptEngineBuildVersion() : "0"
  };
  b.userAgent.q.bc || b.userAgent.q.Sb();
  b.userAgent.q.re = b.userAgent.q.bc ? false : b.userAgent.q.pe;
  b.userAgent.q.Cb = b.userAgent.q.bc ? "0" : b.userAgent.q.tg;
  b.userAgent.q.aa = function(a) {
    return b.d.hc(b.userAgent.q.Cb, a) >= 0
  };
  b.b.i = {};
  (function() {
    function a() {
      return{w:0, ya:0}
    }
    function c() {
      return[]
    }
    function d() {
      function I(za) {
        return j.call(I.src, I.wa, za)
      }
      return I
    }
    function e() {
      return new L
    }
    function f() {
      return new s
    }
    var g = b.userAgent.q.re && !b.userAgent.q.aa("5.7"), j;
    b.b.i.fg = function(I) {
      j = I
    };
    if(g) {
      b.b.i.ta = function() {
        return l.ta()
      };
      b.b.i.xa = function(I) {
        l.xa(I)
      };
      b.b.i.qc = function() {
        return r.ta()
      };
      b.b.i.de = function(I) {
        r.xa(I)
      };
      b.b.i.xf = function() {
        return D.ta()
      };
      b.b.i.ag = function() {
        D.xa(d())
      };
      b.b.i.Nb = function() {
        return y.ta()
      };
      b.b.i.$f = function(I) {
        y.xa(I)
      };
      b.b.i.qf = function() {
        return R.ta()
      };
      b.b.i.Zf = function(I) {
        R.xa(I)
      };
      var l = new B(0, 600);
      l.xb(a);
      var r = new B(0, 600);
      r.xb(c);
      var D = new B(0, 600);
      D.xb(d);
      var y = new B(0, 600);
      y.xb(e);
      var R = new B(0, 600);
      R.xb(f)
    }else {
      b.b.i.ta = a;
      b.b.i.xa = b.db;
      b.b.i.qc = c;
      b.b.i.de = b.db;
      b.b.i.xf = d;
      b.b.i.ag = b.db;
      b.b.i.Nb = e;
      b.b.i.$f = b.db;
      b.b.i.qf = f;
      b.b.i.Zf = b.db
    }
  })();
  b.b.Ra = {};
  b.b.ja = {};
  b.b.Da = {};
  b.b.Zi = "on";
  b.b.Zd = {};
  b.b.vm = "_";
  b.b.ea = function(a, c, d, e, f) {
    if(c) {
      if(b.ia(c)) {
        for(var g = 0;g < c.length;g++) {
          b.b.ea(a, c[g], d, e, f)
        }
        return null
      }else {
        e = !!e;
        var j = b.b.ja;
        c in j || (j[c] = b.b.i.ta());
        j = j[c];
        if(!(e in j)) {
          j[e] = b.b.i.ta();
          j.w++
        }
        j = j[e];
        var l = b.ra(a), r;
        j.ya++;
        if(j[l]) {
          r = j[l];
          for(g = 0;g < r.length;g++) {
            j = r[g];
            if(j.tb == d && j.Ic == f) {
              if(j.eb) {
                break
              }
              return r[g].wa
            }
          }
        }else {
          r = j[l] = b.b.i.qc();
          j.w++
        }
        g = b.b.i.xf();
        g.src = a;
        j = b.b.i.Nb();
        j.bb(d, g, a, c, e, f);
        d = j.wa;
        g.wa = d;
        r.push(j);
        b.b.Ra[d] = j;
        b.b.Da[l] || (b.b.Da[l] = b.b.i.qc());
        b.b.Da[l].push(j);
        if(a.addEventListener) {
          if(a == b.global || !a.Me) {
            a.addEventListener(c, g, e)
          }
        }else {
          a.attachEvent(b.b.uf(c), g)
        }
        return d
      }
    }else {
      throw Error("Invalid event type");
    }
  };
  b.b.sb = function(a, c, d, e, f) {
    if(b.ia(c)) {
      for(var g = 0;g < c.length;g++) {
        b.b.sb(a, c[g], d, e, f)
      }
      return null
    }
    a = b.b.ea(a, c, d, e, f);
    b.b.Ra[a].gc = true;
    return a
  };
  b.b.Xd = function(a, c, d, e, f) {
    c.ea(a, d, e, f)
  };
  b.b.Ea = function(a, c, d, e, f) {
    if(b.ia(c)) {
      for(var g = 0;g < c.length;g++) {
        b.b.Ea(a, c[g], d, e, f)
      }
      return null
    }
    e = !!e;
    a = b.b.Id(a, c, e);
    if(!a) {
      return false
    }
    for(g = 0;g < a.length;g++) {
      if(a[g].tb == d && a[g].capture == e && a[g].Ic == f) {
        return b.b.za(a[g].wa)
      }
    }
    return false
  };
  b.b.za = function(a) {
    if(!b.b.Ra[a]) {
      return false
    }
    var c = b.b.Ra[a];
    if(c.eb) {
      return false
    }
    var d = c.src, e = c.type, f = c.Xf, g = c.capture;
    if(d.removeEventListener) {
      if(d == b.global || !d.Me) {
        d.removeEventListener(e, f, g)
      }
    }else {
      d.detachEvent && d.detachEvent(b.b.uf(e), f)
    }
    d = b.ra(d);
    f = b.b.ja[e][g][d];
    if(b.b.Da[d]) {
      var j = b.b.Da[d];
      b.c.remove(j, c);
      j.length == 0 && delete b.b.Da[d]
    }
    c.eb = true;
    f.Sf = true;
    b.b.Ge(e, g, d, f);
    delete b.b.Ra[a];
    return true
  };
  b.b.je = function(a, c, d, e, f) {
    c.Ea(a, d, e, f)
  };
  b.b.Ge = function(a, c, d, e) {
    if(!e.Oc) {
      if(e.Sf) {
        for(var f = 0, g = 0;f < e.length;f++) {
          if(e[f].eb) {
            var j = e[f].Xf;
            j.src = null;
            b.b.i.ag(j);
            b.b.i.$f(e[f])
          }else {
            if(f != g) {
              e[g] = e[f]
            }
            g++
          }
        }
        e.length = g;
        e.Sf = false;
        if(g == 0) {
          b.b.i.de(e);
          delete b.b.ja[a][c][d];
          b.b.ja[a][c].w--;
          if(b.b.ja[a][c].w == 0) {
            b.b.i.xa(b.b.ja[a][c]);
            delete b.b.ja[a][c];
            b.b.ja[a].w--
          }
          if(b.b.ja[a].w == 0) {
            b.b.i.xa(b.b.ja[a]);
            delete b.b.ja[a]
          }
        }
      }
    }
  };
  b.b.Ia = function(a, c, d) {
    var e = 0, f, g = c == null, j = d == null;
    d = !!d;
    if(a == null) {
      b.object.forEach(b.b.Da, function(r) {
        for(var D = r.length - 1;D >= 0;D--) {
          var y = r[D];
          if((g || c == y.type) && (j || d == y.capture)) {
            b.b.za(y.wa);
            e++
          }
        }
      })
    }else {
      a = b.ra(a);
      if(b.b.Da[a]) {
        a = b.b.Da[a];
        for(f = a.length - 1;f >= 0;f--) {
          var l = a[f];
          if((g || c == l.type) && (j || d == l.capture)) {
            b.b.za(l.wa);
            e++
          }
        }
      }
    }
    return e
  };
  b.b.Kh = function(a, c, d) {
    return b.b.Id(a, c, d) || []
  };
  b.b.Id = function(a, c, d) {
    var e = b.b.ja;
    if(c in e) {
      e = e[c];
      if(d in e) {
        e = e[d];
        a = b.ra(a);
        if(e[a]) {
          return e[a]
        }
      }
    }
    return null
  };
  b.b.Nb = function(a, c, d, e, f) {
    e = !!e;
    if(a = b.b.Id(a, c, e)) {
      for(c = 0;c < a.length;c++) {
        if(a[c].tb == d && a[c].capture == e && a[c].Ic == f) {
          return a[c]
        }
      }
    }
    return null
  };
  b.b.li = function(a, c, d) {
    a = b.ra(a);
    var e = b.b.Da[a];
    if(e) {
      var f = b.Oa(c), g = b.Oa(d);
      if(f && g) {
        e = b.b.ja[c];
        return!!e && !!e[d] && a in e[d]
      }else {
        return f || g ? b.c.some(e, function(j) {
          return f && j.type == c || g && j.capture == d
        }) : true
      }
    }
    return false
  };
  b.b.kh = function(a) {
    var c = [];
    for(var d in a) {
      a[d] && a[d].id ? c.push(d + " = " + a[d] + " (" + a[d].id + ")") : c.push(d + " = " + a[d])
    }
    return c.join("\n")
  };
  var Aa = b.userAgent.j ? "focusin" : "DOMFocusIn", Ba = b.userAgent.j ? "focusout" : "DOMFocusOut";
  b.b.uf = function(a) {
    if(a in b.b.Zd) {
      return b.b.Zd[a]
    }
    return b.b.Zd[a] = b.b.Zi + a
  };
  b.b.oh = function(a, c, d, e) {
    var f = b.b.ja;
    if(c in f) {
      f = f[c];
      if(d in f) {
        return b.b.jb(f[d], a, c, d, e)
      }
    }
    return true
  };
  b.b.jb = function(a, c, d, e, f) {
    var g = 1;
    c = b.ra(c);
    if(a[c]) {
      a.ya--;
      a = a[c];
      if(a.Oc) {
        a.Oc++
      }else {
        a.Oc = 1
      }
      try {
        for(var j = a.length, l = 0;l < j;l++) {
          var r = a[l];
          if(r && !r.eb) {
            g &= b.b.mc(r, f) !== false
          }
        }
      }finally {
        a.Oc--;
        b.b.Ge(d, e, c, a)
      }
    }
    return Boolean(g)
  };
  b.b.mc = function(a, c) {
    c = a.handleEvent(c);
    a.gc && b.b.za(a.wa);
    return c
  };
  b.b.di = function() {
    return b.object.da(b.b.Ra)
  };
  b.b.dispatchEvent = function(a, c) {
    if(b.l(c)) {
      c = new H(c, a)
    }else {
      if(c instanceof H) {
        c.target = c.target || a
      }else {
        var d = c;
        c = new H(c.type, a);
        b.object.extend(c, d)
      }
    }
    d = 1;
    var e, f = c.type, g = b.b.ja;
    if(!(f in g)) {
      return true
    }
    g = g[f];
    f = true in g;
    var j;
    if(f) {
      e = [];
      for(j = a;j;j = j.Kd()) {
        e.push(j)
      }
      j = g[true];
      j.ya = j.w;
      for(var l = e.length - 1;!c.Sa && l >= 0 && j.ya;l--) {
        c.currentTarget = e[l];
        d &= b.b.jb(j, e[l], c.type, true, c) && c.vb != false
      }
    }
    if(false in g) {
      j = g[false];
      j.ya = j.w;
      if(f) {
        for(l = 0;!c.Sa && l < e.length && j.ya;l++) {
          c.currentTarget = e[l];
          d &= b.b.jb(j, e[l], c.type, false, c) && c.vb != false
        }
      }else {
        for(a = a;!c.Sa && a && j.ya;a = a.Kd()) {
          c.currentTarget = a;
          d &= b.b.jb(j, a, c.type, false, c) && c.vb != false
        }
      }
    }
    return Boolean(d)
  };
  b.b.fj = function(a, c) {
    b.b.Hc = a.gj(b.b.Hc, c);
    b.b.i.fg(b.b.Hc)
  };
  b.b.Hc = function(a, c) {
    if(!b.b.Ra[a]) {
      return true
    }
    a = b.b.Ra[a];
    var d = a.type, e = b.b.ja;
    if(!(d in e)) {
      return true
    }
    e = e[d];
    var f, g;
    if(b.userAgent.j) {
      f = c || b.tf("window.event");
      c = true in e;
      var j = false in e;
      if(c) {
        if(b.b.Ii(f)) {
          return true
        }
        b.b.Ti(f)
      }
      var l = b.b.i.qf();
      l.bb(f, this);
      f = true;
      try {
        if(c) {
          for(var r = b.b.i.qc(), D = l.currentTarget;D;D = D.parentNode) {
            r.push(D)
          }
          g = e[true];
          g.ya = g.w;
          for(var y = r.length - 1;!l.Sa && y >= 0 && g.ya;y--) {
            l.currentTarget = r[y];
            f &= b.b.jb(g, r[y], d, true, l)
          }
          if(j) {
            g = e[false];
            g.ya = g.w;
            for(y = 0;!l.Sa && y < r.length && g.ya;y++) {
              l.currentTarget = r[y];
              f &= b.b.jb(g, r[y], d, false, l)
            }
          }
        }else {
          f = b.b.mc(a, l)
        }
      }finally {
        if(r) {
          r.length = 0;
          b.b.i.de(r)
        }
        l.oa();
        b.b.i.Zf(l)
      }
      return f
    }
    g = new s(c, this);
    try {
      f = b.b.mc(a, g)
    }finally {
      g.oa()
    }
    return f
  };
  b.b.i.fg(b.b.Hc);
  b.b.Ti = function(a) {
    var c = false;
    if(a.keyCode == 0) {
      try {
        a.keyCode = -1;
        return
      }catch(d) {
        c = true
      }
    }
    if(c || a.returnValue == undefined) {
      a.returnValue = true
    }
  };
  b.b.Ii = function(a) {
    return a.keyCode < 0 || a.returnValue != undefined
  };
  b.b.dk = 0;
  b.b.ei = function(a) {
    return a + "_" + b.b.dk++
  };
  b.Ba(C, o);
  var ca = new B(0, 100);
  C.prototype.ea = function(a, c, d, e, f) {
    if(b.ia(c)) {
      for(var g = 0;g < c.length;g++) {
        this.ea(a, c[g], d, e, f)
      }
    }else {
      a = b.b.ea(a, c, d || this, e || false, f || this.Qb || this);
      ba(this, a)
    }
    return this
  };
  C.prototype.sb = function(a, c, d, e, f) {
    if(b.ia(c)) {
      for(var g = 0;g < c.length;g++) {
        this.sb(a, c[g], d, e, f)
      }
    }else {
      a = b.b.sb(a, c, d || this, e || false, f || this.Qb || this);
      ba(this, a)
    }
    return this
  };
  C.prototype.Xd = function(a, c, d, e, f) {
    c.ea(a, d, e, f || this.Qb, this);
    return this
  };
  h = C.prototype;
  h.Ea = function(a, c, d, e, f) {
    if(this.Qa || this.m) {
      if(b.ia(c)) {
        for(var g = 0;g < c.length;g++) {
          this.Ea(a, c[g], d, e, f)
        }
      }else {
        if(a = b.b.Nb(a, c, d || this, e || false, f || this.Qb || this)) {
          a = a.wa;
          b.b.za(a);
          if(this.m) {
            b.object.remove(this.m, a)
          }else {
            if(this.Qa == a) {
              this.Qa = null
            }
          }
        }
      }
    }
    return this
  };
  h.je = function(a, c, d, e, f) {
    c.Ea(a, d, e, f || this.Qb, this);
    return this
  };
  h.Ia = function() {
    if(this.m) {
      for(var a in this.m) {
        b.b.za(a);
        delete this.m[a]
      }
      ca.xa(this.m);
      this.m = null
    }else {
      this.Qa && b.b.za(this.Qa)
    }
  };
  h.disposeInternal = function() {
    C.superClass_.disposeInternal.call(this);
    this.Ia()
  };
  h.handleEvent = function() {
    throw Error("EventHandler.handleEvent not implemented");
  };
  b.Ba(G, o);
  h = G.prototype;
  h.Me = true;
  h.$d = null;
  h.Kd = m("$d");
  h.Cj = k("$d");
  h.addEventListener = function(a, c, d, e) {
    b.b.ea(this, a, c, d, e)
  };
  h.removeEventListener = function(a, c, d, e) {
    b.b.Ea(this, a, c, d, e)
  };
  h.dispatchEvent = function(a) {
    return b.b.dispatchEvent(this, a)
  };
  h.disposeInternal = function() {
    G.superClass_.disposeInternal.call(this);
    b.b.Ia(this);
    this.$d = null
  };
  var na = {kl:3, rk:8, fm:9, rl:12, Fk:13, Yl:16, xk:17, nk:18, Ol:19, tk:20, Hk:27, bm:32, Nl:33, Ml:34, Ek:35, bl:36, il:37, jm:38, Ul:39, Ck:40, Ql:44, el:45, Bk:46, sm:48, Jl:49, hm:50, gm:51, Zk:52, Yk:53, $l:54, Xl:55, Dk:56, ol:57, Sl:63, kk:65, pk:66, sk:67, zk:68, E:69, Jk:70, $k:71, al:72, dl:73, fl:74, gl:75, hl:76, jl:77, nl:78, Hl:79, Ll:80, Rl:81, Tl:82, Vl:83, dm:84, im:85, km:86, lm:87, pm:88, qm:89, rm:90, ml:91, wk:93, Gl:96, zl:97, Fl:98, El:99, vl:100, ul:101, Dl:102, Cl:103, 
  tl:104, yl:105, xl:106, Bl:107, wl:109, Al:110, sl:111, Kk:112, Ok:113, Pk:114, Qk:115, Rk:116, Sk:117, Tk:118, Uk:119, Vk:120, Lk:121, Mk:122, Nk:123, ql:144, Wl:186, Ak:189, Gk:187, vk:188, Pl:190, am:191, ok:192, Zl:222, Kl:219, qk:220, uk:221, om:224, ll:224, nm:229};
  b.Ba(K, G);
  h = K.prototype;
  h.Hb = null;
  h.Mc = null;
  h.Vd = null;
  h.Nc = null;
  h.Ub = -1;
  h.qb = -1;
  var oa = {"3":13, "12":144, "63232":38, "63233":40, "63234":37, "63235":39, "63236":112, "63237":113, "63238":114, "63239":115, "63240":116, "63241":117, "63242":118, "63243":119, "63244":120, "63245":121, "63246":122, "63247":123, "63248":44, "63272":46, "63273":36, "63275":35, "63276":33, "63277":34, "63289":144, "63302":45}, pa = {Up:38, Down:40, Left:37, Right:39, Enter:13, F1:112, F2:113, F3:114, F4:115, F5:116, F6:117, F7:118, F8:119, F9:120, F10:121, F11:122, F12:123, "U+007F":46, Home:36, 
  End:35, PageUp:33, PageDown:34, Insert:45}, qa = {61:187, 59:186}, Ca = b.userAgent.j || b.userAgent.ca && b.userAgent.aa("525");
  h = K.prototype;
  h.ji = function(a) {
    if(Ca && !da(a.keyCode, this.Ub, a.shiftKey, a.ctrlKey, a.altKey)) {
      this.handleEvent(a)
    }else {
      this.qb = b.userAgent.la && a.keyCode in qa ? qa[a.keyCode] : a.keyCode
    }
  };
  h.ki = function() {
    this.qb = this.Ub = -1
  };
  h.handleEvent = function(a) {
    var c = a.jf(), d, e;
    if(b.userAgent.j && a.type == "keypress") {
      d = this.qb;
      e = d != 13 && d != 27 ? c.keyCode : 0
    }else {
      if(b.userAgent.ca && a.type == "keypress") {
        d = this.qb;
        e = c.charCode >= 0 && c.charCode < 63232 && P(d) ? c.charCode : 0
      }else {
        if(b.userAgent.fa) {
          d = this.qb;
          e = P(d) ? c.keyCode : 0
        }else {
          d = c.keyCode || this.qb;
          e = c.charCode || 0;
          if(b.userAgent.dc && e == 63 && !d) {
            d = 191
          }
        }
      }
    }
    var f = d, g = c.keyIdentifier;
    if(d) {
      if(d >= 63232 && d in oa) {
        f = oa[d]
      }else {
        if(d == 25 && a.shiftKey) {
          f = 9
        }
      }
    }else {
      if(g && g in pa) {
        f = pa[g]
      }
    }
    a = f == this.Ub;
    this.Ub = f;
    c = new V(f, e, a, c);
    try {
      this.dispatchEvent(c)
    }finally {
      c.oa()
    }
  };
  h.De = function(a) {
    this.Nc && this.detach();
    this.Hb = a;
    this.Mc = b.b.ea(this.Hb, "keypress", this);
    this.Vd = b.b.ea(this.Hb, "keydown", this.ji, false, this);
    this.Nc = b.b.ea(this.Hb, "keyup", this.ki, false, this)
  };
  h.detach = function() {
    if(this.Mc) {
      b.b.za(this.Mc);
      b.b.za(this.Vd);
      b.b.za(this.Nc);
      this.Nc = this.Vd = this.Mc = null
    }
    this.Hb = null;
    this.Ub = -1
  };
  h.disposeInternal = function() {
    K.superClass_.disposeInternal.call(this);
    this.detach()
  };
  b.Ba(V, s);
  b.p = {};
  b.p.vd = function(a) {
    return function() {
      return a
    }
  };
  b.p.ug = b.p.vd(false);
  b.p.Ag = b.p.vd(true);
  b.p.oi = function(a) {
    return a
  };
  b.p.error = function(a) {
    return function() {
      throw Error(a);
    }
  };
  b.p.Si = function(a) {
    return function() {
      return a.call(this)
    }
  };
  b.p.Tg = function() {
    var a = arguments, c = a.length;
    return function() {
      var d;
      if(c) {
        d = a[c - 1].apply(this, arguments)
      }
      for(var e = c - 2;e >= 0;e--) {
        d = a[e].call(this, d)
      }
      return d
    }
  };
  b.p.pj = function() {
    var a = arguments, c = a.length;
    return function() {
      for(var d, e = 0;e < c;e++) {
        d = a[e].apply(this, arguments)
      }
      return d
    }
  };
  b.p.Hg = function() {
    var a = arguments, c = a.length;
    return function() {
      for(var d = 0;d < c;d++) {
        if(!a[d].apply(this, arguments)) {
          return false
        }
      }
      return true
    }
  };
  b.p.$i = function() {
    var a = arguments, c = a.length;
    return function() {
      for(var d = 0;d < c;d++) {
        if(a[d].apply(this, arguments)) {
          return true
        }
      }
      return false
    }
  };
  b.va = {};
  b.va.Oi = function(a) {
    if(/^\s*$/.test(a)) {
      return false
    }
    return/^[\],:{}\s\u2028\u2029]*$/.test(a.replace(/\\["\\\/bfnrtu]/g, "@").replace(/"[^"\\\n\r\u2028\u2029\x00-\x08\x10-\x1f\x80-\x9f]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, "]").replace(/(?:^|:|,)(?:[\s\u2028\u2029]*\[)+/g, ""))
  };
  b.va.parse = function(a) {
    a = String(a);
    if(b.va.Oi(a)) {
      try {
        return eval("(" + a + ")")
      }catch(c) {
      }
    }
    throw Error("Invalid JSON string: " + a);
  };
  b.va.fk = function(a) {
    return eval("(" + a + ")")
  };
  b.va.wb = function(a) {
    return(new O).wb(a)
  };
  O.prototype.wb = function(a) {
    var c = [];
    W(this, a, c);
    return c.join("")
  };
  var X = {'"':'\\"', "\\":"\\\\", "/":"\\/", "\u0008":"\\b", "\u000c":"\\f", "\n":"\\n", "\r":"\\r", "\t":"\\t", "\u000b":"\\u000b"}, ta = /\uffff/.test("\uffff") ? /[\\\"\x00-\x1f\x7f-\uffff]/g : /[\\\"\x00-\x1f\x7f-\xff]/g;
  z.prototype.s = function() {
    return new z(this.top, this.right, this.bottom, this.left)
  };
  if(b.Ab) {
    z.prototype.toString = function() {
      return"(" + this.top + "t, " + this.right + "r, " + this.bottom + "b, " + this.left + "l)"
    }
  }
  z.prototype.contains = function(a) {
    return fa(this, a)
  };
  z.prototype.expand = function(a, c, d, e) {
    if(b.Pa(a)) {
      this.top -= a.top;
      this.right += a.right;
      this.bottom += a.bottom;
      this.left -= a.left
    }else {
      this.top -= a;
      this.right += c;
      this.bottom += d;
      this.left -= e
    }
    return this
  };
  w.prototype.s = function() {
    return new w(this.left, this.top, this.width, this.height)
  };
  w.prototype.Sj = function() {
    return new z(this.top, this.left + this.width, this.top + this.height, this.left)
  };
  if(b.Ab) {
    w.prototype.toString = function() {
      return"(" + this.left + ", " + this.top + " - " + this.width + "w x " + this.height + "h)"
    }
  }
  w.prototype.Sd = function(a) {
    var c = Math.max(this.left, a.left), d = Math.min(this.left + this.width, a.left + a.width);
    if(c <= d) {
      var e = Math.max(this.top, a.top);
      a = Math.min(this.top + this.height, a.top + a.height);
      if(e <= a) {
        this.left = c;
        this.top = e;
        this.width = d - c;
        this.height = a - e;
        return true
      }
    }
    return false
  };
  w.prototype.vi = function(a) {
    return ha(this, a)
  };
  w.prototype.fh = function(a) {
    return ia(this, a)
  };
  w.prototype.Ee = function(a) {
    var c = Math.max(this.left + this.width, a.left + a.width), d = Math.max(this.top + this.height, a.top + a.height);
    this.left = Math.min(this.left, a.left);
    this.top = Math.min(this.top, a.top);
    this.width = c - this.left;
    this.height = d - this.top
  };
  w.prototype.contains = function(a) {
    return a instanceof w ? this.left <= a.left && this.left + this.width >= a.left + a.width && this.top <= a.top && this.top + this.height >= a.top + a.height : a.x >= this.left && a.x <= this.left + this.width && a.y >= this.top && a.y <= this.top + this.height
  };
  w.prototype.Ec = function() {
    return new p(this.width, this.height)
  };
  b.h = {};
  var ra = {pl:0, mk:1, Xk:2, Wk:3, yk:4, Ik:5, cl:6, lk:7, TIMEOUT:8, Il:9};
  b.Ba(F, G);
  F.prototype.enabled = false;
  var M = b.global.window;
  h = F.prototype;
  h.Ja = null;
  h.Fh = m("nb");
  h.setInterval = function(a) {
    this.nb = a;
    if(this.Ja && this.enabled) {
      this.stop();
      this.start()
    }else {
      this.Ja && this.stop()
    }
  };
  h.Qj = function() {
    if(this.enabled) {
      var a = b.now() - this.Wd;
      if(a > 0 && a < this.nb * 0.8) {
        this.Ja = this.Yb.setTimeout(this.pd, this.nb - a)
      }else {
        this.Xe();
        if(this.enabled) {
          this.Ja = this.Yb.setTimeout(this.pd, this.nb);
          this.Wd = b.now()
        }
      }
    }
  };
  h.Xe = function() {
    this.dispatchEvent(sa)
  };
  h.start = function() {
    this.enabled = true;
    if(!this.Ja) {
      this.Ja = this.Yb.setTimeout(this.pd, this.nb);
      this.Wd = b.now()
    }
  };
  h.stop = function() {
    this.enabled = false;
    if(this.Ja) {
      this.Yb.clearTimeout(this.Ja);
      this.Ja = null
    }
  };
  h.disposeInternal = function() {
    F.superClass_.disposeInternal.call(this);
    this.stop();
    delete this.Yb
  };
  var sa = "tick";
  b.g.da = function(a) {
    if(typeof a.da == "function") {
      return a.da()
    }
    if(b.ba(a) || b.l(a)) {
      return a.length
    }
    return b.object.da(a)
  };
  b.g.u = function(a) {
    if(typeof a.u == "function") {
      return a.u()
    }
    if(b.l(a)) {
      return a.split("")
    }
    if(b.ba(a)) {
      for(var c = [], d = a.length, e = 0;e < d;e++) {
        c.push(a[e])
      }
      return c
    }
    return b.object.u(a)
  };
  b.g.ga = function(a) {
    if(typeof a.ga == "function") {
      return a.ga()
    }
    if(typeof a.u != "function") {
      if(b.ba(a) || b.l(a)) {
        var c = [];
        a = a.length;
        for(var d = 0;d < a;d++) {
          c.push(d)
        }
        return c
      }
      return b.object.ga(a)
    }
  };
  b.g.contains = function(a, c) {
    if(typeof a.contains == "function") {
      return a.contains(c)
    }
    if(typeof a.Wa == "function") {
      return a.Wa(c)
    }
    if(b.ba(a) || b.l(a)) {
      return b.c.contains(a, c)
    }
    return b.object.Wa(a, c)
  };
  b.g.z = function(a) {
    if(typeof a.z == "function") {
      return a.z()
    }
    if(b.ba(a) || b.l(a)) {
      return b.c.z(a)
    }
    return b.object.z(a)
  };
  b.g.clear = function(a) {
    if(typeof a.clear == "function") {
      a.clear()
    }else {
      b.ba(a) ? b.c.clear(a) : b.object.clear(a)
    }
  };
  b.g.forEach = function(a, c, d) {
    if(typeof a.forEach == "function") {
      a.forEach(c, d)
    }else {
      if(b.ba(a) || b.l(a)) {
        b.c.forEach(a, c, d)
      }else {
        for(var e = b.g.ga(a), f = b.g.u(a), g = f.length, j = 0;j < g;j++) {
          c.call(d, f[j], e && e[j], a)
        }
      }
    }
  };
  b.g.filter = function(a, c, d) {
    if(typeof a.filter == "function") {
      return a.filter(c, d)
    }
    if(b.ba(a) || b.l(a)) {
      return b.c.filter(a, c, d)
    }
    var e, f = b.g.ga(a), g = b.g.u(a), j = g.length;
    if(f) {
      e = {};
      for(var l = 0;l < j;l++) {
        if(c.call(d, g[l], f[l], a)) {
          e[f[l]] = g[l]
        }
      }
    }else {
      e = [];
      for(l = 0;l < j;l++) {
        c.call(d, g[l], undefined, a) && e.push(g[l])
      }
    }
    return e
  };
  b.g.map = function(a, c, d) {
    if(typeof a.map == "function") {
      return a.map(c, d)
    }
    if(b.ba(a) || b.l(a)) {
      return b.c.map(a, c, d)
    }
    var e, f = b.g.ga(a), g = b.g.u(a), j = g.length;
    if(f) {
      e = {};
      for(var l = 0;l < j;l++) {
        e[f[l]] = c.call(d, g[l], f[l], a)
      }
    }else {
      e = [];
      for(l = 0;l < j;l++) {
        e[l] = c.call(d, g[l], undefined, a)
      }
    }
    return e
  };
  b.g.some = function(a, c, d) {
    if(typeof a.some == "function") {
      return a.some(c, d)
    }
    if(b.ba(a) || b.l(a)) {
      return b.c.some(a, c, d)
    }
    for(var e = b.g.ga(a), f = b.g.u(a), g = f.length, j = 0;j < g;j++) {
      if(c.call(d, f[j], e && e[j], a)) {
        return true
      }
    }
    return false
  };
  b.g.every = function(a, c, d) {
    if(typeof a.every == "function") {
      return a.every(c, d)
    }
    if(b.ba(a) || b.l(a)) {
      return b.c.every(a, c, d)
    }
    for(var e = b.g.ga(a), f = b.g.u(a), g = f.length, j = 0;j < g;j++) {
      if(!c.call(d, f[j], e && e[j], a)) {
        return false
      }
    }
    return true
  };
  b.e = {};
  b.e.yg = b.he;
  b.e.ka = "StopIteration" in b.global ? b.global.StopIteration : Error("StopIteration");
  E.prototype.next = function() {
    throw b.e.ka;
  };
  E.prototype.Aa = function() {
    return this
  };
  b.e.na = function(a) {
    if(a instanceof E) {
      return a
    }
    if(typeof a.Aa == "function") {
      return a.Aa(false)
    }
    if(b.ba(a)) {
      var c = 0, d = new E;
      d.next = function() {
        for(;;) {
          if(c >= a.length) {
            throw b.e.ka;
          }
          if(c in a) {
            return a[c++]
          }else {
            c++
          }
        }
      };
      return d
    }
    throw Error("Not implemented");
  };
  b.e.forEach = function(a, c, d) {
    if(b.ba(a)) {
      try {
        b.c.forEach(a, c, d)
      }catch(e) {
        if(e !== b.e.ka) {
          throw e;
        }
      }
    }else {
      a = b.e.na(a);
      try {
        for(;;) {
          c.call(d, a.next(), undefined, a)
        }
      }catch(f) {
        if(f !== b.e.ka) {
          throw f;
        }
      }
    }
  };
  b.e.filter = function(a, c, d) {
    a = b.e.na(a);
    var e = new E;
    e.next = function() {
      for(;;) {
        var f = a.next();
        if(c.call(d, f, undefined, a)) {
          return f
        }
      }
    };
    return e
  };
  b.e.ij = function(a, c, d) {
    var e = 0, f = a, g = d || 1;
    if(arguments.length > 1) {
      e = a;
      f = c
    }
    if(g == 0) {
      throw Error("Range step argument must not be zero");
    }
    var j = new E;
    j.next = function() {
      if(g > 0 && e >= f || g < 0 && e <= f) {
        throw b.e.ka;
      }
      var l = e;
      e += g;
      return l
    };
    return j
  };
  b.e.join = function(a, c) {
    return b.e.yb(a).join(c)
  };
  b.e.map = function(a, c, d) {
    a = b.e.na(a);
    var e = new E;
    e.next = function() {
      for(;;) {
        var f = a.next();
        return c.call(d, f, undefined, a)
      }
    };
    return e
  };
  b.e.reduce = function(a, c, d, e) {
    var f = d;
    b.e.forEach(a, function(g) {
      f = c.call(e, f, g)
    });
    return f
  };
  b.e.some = function(a, c, d) {
    a = b.e.na(a);
    try {
      for(;;) {
        if(c.call(d, a.next(), undefined, a)) {
          return true
        }
      }
    }catch(e) {
      if(e !== b.e.ka) {
        throw e;
      }
    }
    return false
  };
  b.e.every = function(a, c, d) {
    a = b.e.na(a);
    try {
      for(;;) {
        if(!c.call(d, a.next(), undefined, a)) {
          return false
        }
      }
    }catch(e) {
      if(e !== b.e.ka) {
        throw e;
      }
    }
    return true
  };
  b.e.Pg = function() {
    var a = arguments, c = a.length, d = 0, e = new E;
    e.next = function() {
      try {
        if(d >= c) {
          throw b.e.ka;
        }
        return b.e.na(a[d]).next()
      }catch(f) {
        if(f !== b.e.ka || d >= c) {
          throw f;
        }else {
          d++;
          return this.next()
        }
      }
    };
    return e
  };
  b.e.gh = function(a, c, d) {
    a = b.e.na(a);
    var e = new E, f = true;
    e.next = function() {
      for(;;) {
        var g = a.next();
        if(!(f && c.call(d, g, undefined, a))) {
          f = false;
          return g
        }
      }
    };
    return e
  };
  b.e.Pj = function(a, c, d) {
    a = b.e.na(a);
    var e = new E, f = true;
    e.next = function() {
      for(;;) {
        if(f) {
          var g = a.next();
          if(c.call(d, g, undefined, a)) {
            return g
          }else {
            f = false
          }
        }else {
          throw b.e.ka;
        }
      }
    };
    return e
  };
  b.e.yb = function(a) {
    if(b.ba(a)) {
      return b.c.yb(a)
    }
    a = b.e.na(a);
    var c = [];
    b.e.forEach(a, function(d) {
      c.push(d)
    });
    return c
  };
  b.e.Ma = function(a, c) {
    a = b.e.na(a);
    c = b.e.na(c);
    var d, e;
    try {
      for(;;) {
        d = e = false;
        var f = a.next();
        d = true;
        var g = c.next();
        e = true;
        if(f != g) {
          return false
        }
      }
    }catch(j) {
      if(j !== b.e.ka) {
        throw j;
      }else {
        if(d && !e) {
          return false
        }
        if(!e) {
          try {
            c.next();
            return false
          }catch(l) {
            if(l !== b.e.ka) {
              throw l;
            }
            return true
          }
        }
      }
    }
    return false
  };
  b.e.Vi = function(a, c) {
    try {
      return b.e.na(a).next()
    }catch(d) {
      if(d != b.e.ka) {
        throw d;
      }
      return c
    }
  };
  h = v.prototype;
  h.w = 0;
  h.Zb = 0;
  h.da = m("w");
  h.u = function() {
    Q(this);
    for(var a = [], c = 0;c < this.m.length;c++) {
      a.push(this.r[this.m[c]])
    }
    return a
  };
  h.ga = function() {
    Q(this);
    return this.m.concat()
  };
  h.Eb = function(a) {
    return N(this.r, a)
  };
  h.Wa = function(a) {
    for(var c = 0;c < this.m.length;c++) {
      var d = this.m[c];
      if(N(this.r, d) && this.r[d] == a) {
        return true
      }
    }
    return false
  };
  h.Ma = function(a, c) {
    if(this === a) {
      return true
    }
    if(this.w != a.da()) {
      return false
    }
    c = c || ja;
    Q(this);
    for(var d, e = 0;d = this.m[e];e++) {
      if(!c(this.qa(d), a.qa(d))) {
        return false
      }
    }
    return true
  };
  v.prototype.z = function() {
    return this.w == 0
  };
  v.prototype.clear = function() {
    this.r = {};
    this.Zb = this.w = this.m.length = 0
  };
  v.prototype.remove = function(a) {
    if(N(this.r, a)) {
      delete this.r[a];
      this.w--;
      this.Zb++;
      this.m.length > 2 * this.w && Q(this);
      return true
    }
    return false
  };
  h = v.prototype;
  h.qa = function(a, c) {
    if(N(this.r, a)) {
      return this.r[a]
    }
    return c
  };
  h.sa = function(a, c) {
    if(!N(this.r, a)) {
      this.w++;
      this.m.push(a);
      this.Zb++
    }
    this.r[a] = c
  };
  h.Db = function(a) {
    var c;
    if(a instanceof v) {
      c = a.ga();
      a = a.u()
    }else {
      c = b.object.ga(a);
      a = b.object.u(a)
    }
    for(var d = 0;d < c.length;d++) {
      this.sa(c[d], a[d])
    }
  };
  h.s = function() {
    return new v(this)
  };
  h.fe = function() {
    for(var a = new v, c = 0;c < this.m.length;c++) {
      var d = this.m[c];
      a.sa(this.r[d], d)
    }
    return a
  };
  h.Gh = function() {
    return this.Aa(true)
  };
  h.fi = function() {
    return this.Aa(false)
  };
  h.Aa = function(a) {
    Q(this);
    var c = 0, d = this.m, e = this.r, f = this.Zb, g = this, j = new E;
    j.next = function() {
      for(;;) {
        if(f != g.Zb) {
          throw Error("The map has changed since the iterator was created");
        }
        if(c >= d.length) {
          throw b.e.ka;
        }
        var l = d[c++];
        return a ? l : e[l]
      }
    };
    return j
  };
  h = x.prototype;
  h.da = function() {
    return this.r.da()
  };
  h.add = function(a) {
    this.r.sa(Y(a), a)
  };
  h.Db = function(a) {
    a = b.g.u(a);
    for(var c = a.length, d = 0;d < c;d++) {
      this.add(a[d])
    }
  };
  h.Ia = function(a) {
    a = b.g.u(a);
    for(var c = a.length, d = 0;d < c;d++) {
      this.remove(a[d])
    }
  };
  h.remove = function(a) {
    return this.r.remove(Y(a))
  };
  h.clear = function() {
    this.r.clear()
  };
  h.z = function() {
    return this.r.z()
  };
  h.contains = function(a) {
    return this.r.Eb(Y(a))
  };
  h.Ug = function(a) {
    return b.g.every(a, this.contains, this)
  };
  h.Sd = function(a) {
    var c = new x;
    a = b.g.u(a);
    for(var d = 0;d < a.length;d++) {
      var e = a[d];
      this.contains(e) && c.add(e)
    }
    return c
  };
  h.u = function() {
    return this.r.u()
  };
  h.s = function() {
    return new x(this)
  };
  h.Ma = function(a) {
    return this.da() == b.g.da(a) && this.If(a)
  };
  h.If = function(a) {
    var c = b.g.da(a);
    if(this.da() > c) {
      return false
    }
    if(!(a instanceof x) && c > 5) {
      a = new x(a)
    }
    return b.g.every(this, function(d) {
      return b.g.contains(a, d)
    })
  };
  h.Aa = function() {
    return this.r.Aa(false)
  };
  b.h.n = function() {
    return b.h.n.af()
  };
  b.h.n.vf = function() {
    return b.h.n.qd || (b.h.n.qd = b.h.n.Wf())
  };
  b.h.n.af = null;
  b.h.n.Wf = null;
  b.h.n.qd = null;
  b.h.n.dg = function(a, c) {
    b.h.n.af = a;
    b.h.n.Wf = c;
    b.h.n.qd = null
  };
  b.h.n.Xg = function() {
    var a = b.h.n.wf();
    return a ? new ActiveXObject(a) : new XMLHttpRequest
  };
  b.h.n.Yg = function() {
    var a = {};
    if(b.h.n.wf()) {
      a[aa] = true;
      a[$] = true
    }
    return a
  };
  b.h.n.dg(b.h.n.Xg, b.h.n.Yg);
  var aa = 0, $ = 1;
  b.h.n.Pd = null;
  b.h.n.wf = function() {
    if(!b.h.n.Pd && typeof XMLHttpRequest == "undefined" && typeof ActiveXObject != "undefined") {
      for(var a = ["MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "Microsoft.XMLHTTP"], c = 0;c < a.length;c++) {
        var d = a[c];
        try {
          new ActiveXObject(d);
          return b.h.n.Pd = d
        }catch(e) {
        }
      }
      throw Error("Could not create ActiveXObject. ActiveX might be disabled, or MSXML might not be installed");
    }
    return b.h.n.Pd
  };
  h = J.prototype;
  h.ib = b.userAgent.la;
  h.vj = function(a) {
    this.ib = b.userAgent.la && a
  };
  h.be = function(a) {
    this.ib && this.Sc.push(Z(a))
  };
  h.ae = function() {
    if(this.ib) {
      var a = this.Sc.pop();
      ua(this, a)
    }
  };
  h.Ci = function(a) {
    if(!this.ib) {
      return true
    }
    return!this.Ka[Z(a)]
  };
  h.Pf = function(a) {
    if(this.ib) {
      a = b.ra(a);
      for(var c = 0;c < this.Sc.length;c++) {
        var d = this.Sc[c];
        S(this, this.Ka, d, a);
        S(this, this.Vc, a, d)
      }
    }
  };
  h.Of = function(a) {
    if(this.ib) {
      a = b.ra(a);
      delete this.Vc[a];
      for(var c in this.Ka) {
        b.c.remove(this.Ka[c], a);
        this.Ka[c].length == 0 && delete this.Ka[c]
      }
    }
  };
  b.h.gb = new J;
  b.Ba(t, G);
  var T = [];
  h = t.prototype;
  h.Fa = false;
  h.o = null;
  h.Uc = null;
  h.Lf = "";
  h.Pi = "";
  h.rb = 0;
  h.cb = "";
  h.Cd = false;
  h.Jc = false;
  h.Qd = false;
  h.ab = false;
  h.Xb = 0;
  h.fb = null;
  h.ci = m("Xb");
  h.hg = function(a) {
    this.Xb = Math.max(0, a)
  };
  h.send = function(a, c, d, e) {
    if(this.Fa) {
      throw Error("[goog.net.XhrIo] Object is active with another request");
    }
    c = c || "GET";
    this.Lf = a;
    this.cb = "";
    this.rb = 0;
    this.Pi = c;
    this.Cd = false;
    this.Fa = true;
    this.o = new b.h.n;
    this.Uc = b.h.n.vf();
    b.h.gb.Pf(this.o);
    this.o.onreadystatechange = b.Ga(this.Vf, this);
    try {
      this.Qd = true;
      this.o.open(c, a, true);
      this.Qd = false
    }catch(f) {
      ka(this, 5, f);
      return
    }
    a = d || "";
    var g = this.headers.s();
    e && b.g.forEach(e, function(l, r) {
      g.sa(r, l)
    });
    c == "POST" && !g.Eb("Content-Type") && g.sa("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
    b.g.forEach(g, function(l, r) {
      this.o.setRequestHeader(r, l)
    }, this);
    try {
      if(this.fb) {
        M.clearTimeout(this.fb);
        this.fb = null
      }
      if(this.Xb > 0) {
        this.fb = M.setTimeout(b.Ga(this.Rj, this), this.Xb)
      }
      this.Jc = true;
      this.o.send(a);
      this.Jc = false
    }catch(j) {
      ka(this, 5, j)
    }
  };
  h.dispatchEvent = function(a) {
    if(this.o) {
      b.h.gb.be(this.o);
      try {
        return t.superClass_.dispatchEvent.call(this, a)
      }finally {
        b.h.gb.ae()
      }
    }else {
      return t.superClass_.dispatchEvent.call(this, a)
    }
  };
  h.Rj = function() {
    if(typeof b != "undefined") {
      if(this.o) {
        this.cb = "Timed out after " + this.Xb + "ms, aborting";
        this.rb = 8;
        this.dispatchEvent("timeout");
        this.abort(8)
      }
    }
  };
  t.prototype.abort = function(a) {
    if(this.o) {
      this.Fa = false;
      this.ab = true;
      this.o.abort();
      this.ab = false;
      this.rb = a || 7;
      this.dispatchEvent("complete");
      this.dispatchEvent("abort");
      U(this)
    }
  };
  t.prototype.disposeInternal = function() {
    if(this.o) {
      if(this.Fa) {
        this.Fa = false;
        this.ab = true;
        this.o.abort();
        this.ab = false
      }
      U(this, true)
    }
    t.superClass_.disposeInternal.call(this)
  };
  t.prototype.Vf = function() {
    !this.Qd && !this.Jc && !this.ab ? this.Yd() : ma(this)
  };
  t.prototype.Yd = function() {
    ma(this)
  };
  h = t.prototype;
  h.wi = m("Fa");
  h.Td = function() {
    return this.lb() == 4
  };
  h.Jf = function() {
    switch(this.Fc()) {
      case 0:
      ;
      case 200:
      ;
      case 204:
      ;
      case 304:
        return true;
      default:
        return false
    }
  };
  h.lb = function() {
    return this.o ? this.o.readyState : 0
  };
  h.Fc = function() {
    try {
      return this.lb() > 2 ? this.o.status : -1
    }catch(a) {
      return-1
    }
  };
  h.yf = function() {
    try {
      return this.lb() > 2 ? this.o.statusText : ""
    }catch(a) {
      return""
    }
  };
  h.Jh = function() {
    return String(this.Lf)
  };
  h.Zh = function() {
    return this.o ? this.o.responseText : ""
  };
  h.$h = function() {
    return this.o ? this.o.responseXML : null
  };
  h.Yh = function() {
    return this.o ? b.va.parse(this.o.responseText) : undefined
  };
  h.getResponseHeader = function(a) {
    return this.o && this.Td() ? this.o.getResponseHeader(a) : undefined
  };
  h.Ih = m("rb");
  h.Hh = function() {
    return b.l(this.cb) ? this.cb : String(this.cb)
  };
  b.style = {};
  b.style.Fj = function(a, c, d) {
    b.l(c) ? b.style.gg(a, d, c) : b.object.forEach(c, b.Pc(b.style.gg, a))
  };
  b.style.gg = function(a, c, d) {
    a.style[b.style.Tc(d)] = c
  };
  b.style.bi = function(a, c) {
    return a.style[b.style.Tc(c)]
  };
  b.style.getComputedStyle = function(a, c) {
    var d = b.a.t(a);
    if(d.defaultView && d.defaultView.getComputedStyle) {
      if(a = d.defaultView.getComputedStyle(a, "")) {
        return a[c]
      }
    }
    return null
  };
  b.style.Kb = function(a, c) {
    return a.currentStyle ? a.currentStyle[c] : null
  };
  b.style.ha = function(a, c) {
    return b.style.getComputedStyle(a, c) || b.style.Kb(a, c) || a.style[c]
  };
  b.style.mf = function(a) {
    return b.style.ha(a, "position")
  };
  b.style.sh = function(a) {
    return b.style.ha(a, "backgroundColor")
  };
  b.style.vh = function(a) {
    return b.style.ha(a, "overflowX")
  };
  b.style.wh = function(a) {
    return b.style.ha(a, "overflowY")
  };
  b.style.yh = function(a) {
    return b.style.ha(a, "zIndex")
  };
  b.style.xh = function(a) {
    return b.style.ha(a, "textAlign")
  };
  b.style.uh = function(a) {
    return b.style.ha(a, "cursor")
  };
  b.style.eg = function(a, c, d) {
    var e, f = b.userAgent.la && (b.userAgent.dc || b.userAgent.ze) && b.userAgent.aa("1.9");
    if(c instanceof q) {
      e = c.x;
      c = c.y
    }else {
      e = c;
      c = d
    }
    a.style.left = typeof e == "number" ? (f ? Math.round(e) : e) + "px" : e;
    a.style.top = typeof c == "number" ? (f ? Math.round(c) : c) + "px" : c
  };
  b.style.Uh = function(a) {
    return new q(a.offsetLeft, a.offsetTop)
  };
  b.style.lf = function(a) {
    a = a ? a.nodeType == 9 ? a : b.a.t(a) : b.a.Na();
    if(b.userAgent.j && !b.a.ma(a).Ca()) {
      return a.body
    }
    return a.documentElement
  };
  b.style.gf = function(a) {
    var c = a.getBoundingClientRect();
    if(b.userAgent.j) {
      a = a.ownerDocument;
      c.left -= a.documentElement.clientLeft + a.body.clientLeft;
      c.top -= a.documentElement.clientTop + a.body.clientTop
    }
    return c
  };
  b.style.Jd = function(a) {
    if(b.userAgent.j) {
      return a.offsetParent
    }
    var c = b.a.t(a), d = b.style.ha(a, "position"), e = d == "fixed" || d == "absolute";
    for(a = a.parentNode;a && a != c;a = a.parentNode) {
      d = b.style.ha(a, "position");
      e = e && d == "static" && a != c.documentElement && a != c.body;
      if(!e && (a.scrollWidth > a.clientWidth || a.scrollHeight > a.clientHeight || d == "fixed" || d == "absolute")) {
        return a
      }
    }
    return null
  };
  b.style.gi = function(a) {
    var c = new z(0, Infinity, Infinity, 0), d = b.a.ma(a), e = d.sc(), f;
    for(a = a;a = b.style.Jd(a);) {
      if((!b.userAgent.j || a.clientWidth != 0) && (a.scrollWidth != a.clientWidth || a.scrollHeight != a.clientHeight) && b.style.ha(a, "overflow") != "visible") {
        var g = b.style.ua(a), j = b.style.kf(a);
        g.x += j.x;
        g.y += j.y;
        c.top = Math.max(c.top, g.y);
        c.right = Math.min(c.right, g.x + a.clientWidth);
        c.bottom = Math.min(c.bottom, g.y + a.clientHeight);
        c.left = Math.max(c.left, g.x);
        f = f || a != e
      }
    }
    a = e.scrollLeft;
    e = e.scrollTop;
    if(b.userAgent.ca) {
      c.left += a;
      c.top += e
    }else {
      c.left = Math.max(c.left, a);
      c.top = Math.max(c.top, e)
    }
    if(!f || b.userAgent.ca) {
      c.right += a;
      c.bottom += e
    }
    d = d.Pb();
    c.right = Math.min(c.right, a + d.width);
    c.bottom = Math.min(c.bottom, e + d.height);
    return c.top >= 0 && c.left >= 0 && c.bottom > c.top && c.right > c.left ? c : null
  };
  b.style.oj = function(a, c, d) {
    var e = b.style.ua(a), f = b.style.ua(c), g = b.style.Jb(c), j = e.x - f.x - g.left;
    e = e.y - f.y - g.top;
    f = c.clientWidth - a.offsetWidth;
    a = c.clientHeight - a.offsetHeight;
    if(d) {
      c.scrollLeft += j - f / 2;
      c.scrollTop += e - a / 2
    }else {
      c.scrollLeft += Math.min(j, Math.max(j - f, 0));
      c.scrollTop += Math.min(e, Math.max(e - a, 0))
    }
  };
  b.style.kf = function(a) {
    if(b.userAgent.la && !b.userAgent.aa("1.9")) {
      var c = parseFloat(b.style.getComputedStyle(a, "borderLeftWidth"));
      if(b.style.Hf(a)) {
        var d = a.offsetWidth - a.clientWidth - c - parseFloat(b.style.getComputedStyle(a, "borderRightWidth"));
        c += d
      }
      return new q(c, parseFloat(b.style.getComputedStyle(a, "borderTopWidth")))
    }
    return new q(a.clientLeft, a.clientTop)
  };
  b.style.ua = function(a) {
    var c, d = b.a.t(a), e = b.style.ha(a, "position"), f = b.userAgent.la && d.getBoxObjectFor && !a.getBoundingClientRect && e == "absolute" && (c = d.getBoxObjectFor(a)) && (c.screenX < 0 || c.screenY < 0), g = new q(0, 0), j = b.style.lf(d);
    if(a == j) {
      return g
    }
    if(a.getBoundingClientRect) {
      c = b.style.gf(a);
      a = b.a.ma(d).Ya();
      g.x = c.left + a.x;
      g.y = c.top + a.y
    }else {
      if(d.getBoxObjectFor && !f) {
        c = d.getBoxObjectFor(a);
        a = d.getBoxObjectFor(j);
        g.x = c.screenX - a.screenX;
        g.y = c.screenY - a.screenY
      }else {
        c = a;
        do {
          g.x += c.offsetLeft;
          g.y += c.offsetTop;
          if(c != a) {
            g.x += c.clientLeft || 0;
            g.y += c.clientTop || 0
          }
          if(b.userAgent.ca && b.style.mf(c) == "fixed") {
            g.x += d.body.scrollLeft;
            g.y += d.body.scrollTop;
            break
          }
          c = c.offsetParent
        }while(c && c != a);
        if(b.userAgent.fa || b.userAgent.ca && e == "absolute") {
          g.y -= d.body.offsetTop
        }
        for(c = a;(c = b.style.Jd(c)) && c != d.body && c != j;) {
          g.x -= c.scrollLeft;
          if(!b.userAgent.fa || c.tagName != "TR") {
            g.y -= c.scrollTop
          }
        }
      }
    }
    return g
  };
  b.style.Rh = function(a) {
    return b.style.ua(a).x
  };
  b.style.Sh = function(a) {
    return b.style.ua(a).y
  };
  b.style.rf = function(a, c) {
    var d = new q(0, 0), e = b.a.$a(b.a.t(a));
    a = a;
    do {
      var f = e == c ? b.style.ua(a) : b.style.rc(a);
      d.x += f.x;
      d.y += f.y
    }while(e && e != c && (a = e.frameElement) && (e = e.parent));
    return d
  };
  b.style.Xj = function(a, c, d) {
    if(c.Na() != d.Na()) {
      var e = c.Na().body;
      d = b.style.rf(e, d.$a());
      d = A(d, b.style.ua(e));
      if(b.userAgent.j && !c.Ca()) {
        d = A(d, c.Ya())
      }
      a.left += d.x;
      a.top += d.y
    }
  };
  b.style.Xh = function(a, c) {
    a = b.style.rc(a);
    c = b.style.rc(c);
    return new q(a.x - c.x, a.y - c.y)
  };
  b.style.rc = function(a) {
    var c = new q;
    if(a.nodeType == 1) {
      if(a.getBoundingClientRect) {
        var d = b.style.gf(a);
        c.x = d.left;
        c.y = d.top
      }else {
        d = b.a.ma(a).Ya();
        a = b.style.ua(a);
        c.x = a.x - d.x;
        c.y = a.y - d.y
      }
    }else {
      c.x = a.clientX;
      c.y = a.clientY
    }
    return c
  };
  b.style.Bj = function(a, c, d) {
    var e = b.style.ua(a);
    if(c instanceof q) {
      d = c.y;
      c = c.x
    }
    b.style.eg(a, a.offsetLeft + (c - e.x), a.offsetTop + (d - e.y))
  };
  b.style.Ej = function(a, c, d) {
    if(c instanceof p) {
      d = c.height;
      c = c.width
    }else {
      if(d == undefined) {
        throw Error("missing height argument");
      }
      d = d
    }
    a.style.width = typeof c == "number" ? Math.round(c) + "px" : c;
    a.style.height = typeof d == "number" ? Math.round(d) + "px" : d
  };
  b.style.Ec = function(a) {
    var c = b.userAgent.fa && !b.userAgent.aa("10");
    if(b.style.ha(a, "display") != "none") {
      return c ? new p(a.offsetWidth || a.clientWidth, a.offsetHeight || a.clientHeight) : new p(a.offsetWidth, a.offsetHeight)
    }
    var d = a.style, e = d.display, f = d.visibility, g = d.position;
    d.visibility = "hidden";
    d.position = "absolute";
    d.display = "inline";
    if(c) {
      c = a.offsetWidth || a.clientWidth;
      a = a.offsetHeight || a.clientHeight
    }else {
      c = a.offsetWidth;
      a = a.offsetHeight
    }
    d.display = e;
    d.position = g;
    d.visibility = f;
    return new p(c, a)
  };
  b.style.th = function(a) {
    var c = b.style.ua(a);
    a = b.style.Ec(a);
    return new w(c.x, c.y, a.width, a.height)
  };
  b.style.jg = {};
  b.style.Tc = function(a) {
    return b.style.jg[a] || (b.style.jg[a] = String(a).replace(/\-([a-z])/g, function(c, d) {
      return d.toUpperCase()
    }))
  };
  b.style.kg = function(a) {
    return a.replace(/([A-Z])/g, "-$1").toLowerCase()
  };
  b.style.Ph = function(a) {
    var c = a.style;
    a = "";
    if("opacity" in c) {
      a = c.opacity
    }else {
      if("MozOpacity" in c) {
        a = c.MozOpacity
      }else {
        if("filter" in c) {
          if(c = c.filter.match(/alpha\(opacity=([\d.]+)\)/)) {
            a = String(c[1] / 100)
          }
        }
      }
    }
    return a == "" ? a : Number(a)
  };
  b.style.Aj = function(a, c) {
    a = a.style;
    if("opacity" in a) {
      a.opacity = c
    }else {
      if("MozOpacity" in a) {
        a.MozOpacity = c
      }else {
        if("filter" in a) {
          a.filter = c === "" ? "" : "alpha(opacity=" + c * 100 + ")"
        }
      }
    }
  };
  b.style.Gj = function(a, c) {
    a = a.style;
    if(b.userAgent.j && !b.userAgent.aa("8")) {
      a.filter = 'progid:DXImageTransform.Microsoft.AlphaImageLoader(src="' + c + '", sizingMethod="crop")'
    }else {
      a.backgroundImage = "url(" + c + ")";
      a.backgroundPosition = "top left";
      a.backgroundRepeat = "no-repeat"
    }
  };
  b.style.Qg = function(a) {
    a = a.style;
    if("filter" in a) {
      a.filter = ""
    }else {
      a.backgroundImage = "none"
    }
  };
  b.style.Ij = function(a, c) {
    a.style.display = c ? "" : "none"
  };
  b.style.Fi = function(a) {
    return a.style.display != "none"
  };
  b.style.ti = function(a, c) {
    c = b.a.ma(c);
    var d = null;
    if(b.userAgent.j) {
      d = c.Na().createStyleSheet();
      b.style.ee(d, a)
    }else {
      var e = c.Za("head")[0];
      if(!e) {
        d = c.Za("body")[0];
        e = c.La("head");
        d.parentNode.insertBefore(e, d)
      }
      d = c.La("style");
      b.style.ee(d, a);
      c.appendChild(e, d)
    }
    return d
  };
  b.style.ck = function(a) {
    b.a.removeNode(a.ownerNode || a.owningElement || a)
  };
  b.style.ee = function(a, c) {
    if(b.userAgent.j) {
      a.cssText = c
    }else {
      a[b.userAgent.ca ? "innerText" : "innerHTML"] = c
    }
  };
  b.style.Dj = function(a) {
    a = a.style;
    if(b.userAgent.j && !b.userAgent.aa("8")) {
      a.whiteSpace = "pre";
      a.wordWrap = "break-word"
    }else {
      a.whiteSpace = b.userAgent.la ? "-moz-pre-wrap" : b.userAgent.fa ? "-o-pre-wrap" : "pre-wrap"
    }
  };
  b.style.zj = function(a) {
    a = a.style;
    a.position = "relative";
    if(b.userAgent.j && !b.userAgent.aa("8")) {
      a.zoom = "1";
      a.display = "inline"
    }else {
      a.display = b.userAgent.la ? b.userAgent.aa("1.9a") ? "inline-block" : "-moz-inline-box" : "inline-block"
    }
  };
  b.style.Hf = function(a) {
    return"rtl" == b.style.ha(a, "direction")
  };
  b.style.ke = b.userAgent.la ? "MozUserSelect" : b.userAgent.ca ? "WebkitUserSelect" : null;
  b.style.Ni = function(a) {
    if(b.style.ke) {
      return a.style[b.style.ke].toLowerCase() == "none"
    }else {
      if(b.userAgent.j || b.userAgent.fa) {
        return a.getAttribute("unselectable") == "on"
      }
    }
    return false
  };
  b.style.Hj = function(a, c, d) {
    d = !d ? a.getElementsByTagName("*") : null;
    var e = b.style.ke;
    if(e) {
      c = c ? "none" : "";
      a.style[e] = c;
      if(d) {
        a = 0;
        for(var f;f = d[a];a++) {
          f.style[e] = c
        }
      }
    }else {
      if(b.userAgent.j || b.userAgent.fa) {
        c = c ? "on" : "";
        a.setAttribute("unselectable", c);
        if(d) {
          for(a = 0;f = d[a];a++) {
            f.setAttribute("unselectable", c)
          }
        }
      }
    }
  };
  b.style.ff = function(a) {
    return new p(a.offsetWidth, a.offsetHeight)
  };
  b.style.qj = function(a, c) {
    var d = b.a.t(a), e = b.a.ma(d).Ca();
    if(b.userAgent.j && (!e || !b.userAgent.aa("8"))) {
      d = a.style;
      if(e) {
        e = b.style.Cc(a);
        a = b.style.Jb(a);
        d.pixelWidth = c.width - a.left - e.left - e.right - a.right;
        d.pixelHeight = c.height - a.top - e.top - e.bottom - a.bottom
      }else {
        d.pixelWidth = c.width;
        d.pixelHeight = c.height
      }
    }else {
      b.style.cg(a, c, "border-box")
    }
  };
  b.style.zh = function(a) {
    var c = b.a.t(a), d = b.userAgent.j && a.currentStyle;
    if(d && b.a.ma(c).Ca() && d.width != "auto" && d.height != "auto" && !d.boxSizing) {
      c = b.style.kb(a, d.width, "width", "pixelWidth");
      a = b.style.kb(a, d.height, "height", "pixelHeight");
      return new p(c, a)
    }else {
      d = b.style.ff(a);
      c = b.style.Cc(a);
      a = b.style.Jb(a);
      return new p(d.width - a.left - c.left - c.right - a.right, d.height - a.top - c.top - c.bottom - a.bottom)
    }
  };
  b.style.rj = function(a, c) {
    var d = b.a.t(a), e = b.a.ma(d).Ca();
    if(b.userAgent.j && (!e || !b.userAgent.aa("8"))) {
      d = a.style;
      if(e) {
        d.pixelWidth = c.width;
        d.pixelHeight = c.height
      }else {
        e = b.style.Cc(a);
        a = b.style.Jb(a);
        d.pixelWidth = c.width + a.left + e.left + e.right + a.right;
        d.pixelHeight = c.height + a.top + e.top + e.bottom + a.bottom
      }
    }else {
      b.style.cg(a, c, "content-box")
    }
  };
  b.style.cg = function(a, c, d) {
    a = a.style;
    if(b.userAgent.la) {
      a.MozBoxSizing = d
    }else {
      if(b.userAgent.ca) {
        a.WebkitBoxSizing = d
      }else {
        if(b.userAgent.fa && !b.userAgent.aa("9.50")) {
          d ? a.setProperty("box-sizing", d) : a.removeProperty("box-sizing")
        }else {
          a.boxSizing = d
        }
      }
    }
    a.width = c.width + "px";
    a.height = c.height + "px"
  };
  b.style.kb = function(a, c, d, e) {
    if(/^\d+px?$/.test(c)) {
      return parseInt(c, 10)
    }else {
      var f = a.style[d], g = a.runtimeStyle[d];
      a.runtimeStyle[d] = a.currentStyle[d];
      a.style[d] = c;
      c = a.style[e];
      a.style[d] = f;
      a.runtimeStyle[d] = g;
      return c
    }
  };
  b.style.wc = function(a, c) {
    return b.style.kb(a, b.style.Kb(a, c), "left", "pixelLeft")
  };
  b.style.hf = function(a, c) {
    if(b.userAgent.j) {
      var d = b.style.wc(a, c + "Left"), e = b.style.wc(a, c + "Right"), f = b.style.wc(a, c + "Top");
      a = b.style.wc(a, c + "Bottom");
      return new z(f, e, a, d)
    }else {
      d = b.style.getComputedStyle(a, c + "Left");
      e = b.style.getComputedStyle(a, c + "Right");
      f = b.style.getComputedStyle(a, c + "Top");
      a = b.style.getComputedStyle(a, c + "Bottom");
      return new z(parseFloat(f), parseFloat(e), parseFloat(a), parseFloat(d))
    }
  };
  b.style.Cc = function(a) {
    return b.style.hf(a, "padding")
  };
  b.style.Mh = function(a) {
    return b.style.hf(a, "margin")
  };
  b.style.Cf = {thin:2, medium:4, thick:6};
  b.style.vc = function(a, c) {
    if(b.style.Kb(a, c + "Style") == "none") {
      return 0
    }
    c = b.style.Kb(a, c + "Width");
    if(c in b.style.Cf) {
      return b.style.Cf[c]
    }
    return b.style.kb(a, c, "left", "pixelLeft")
  };
  b.style.Jb = function(a) {
    if(b.userAgent.j) {
      var c = b.style.vc(a, "borderLeft"), d = b.style.vc(a, "borderRight"), e = b.style.vc(a, "borderTop");
      a = b.style.vc(a, "borderBottom");
      return new z(e, d, a, c)
    }else {
      c = b.style.getComputedStyle(a, "borderLeftWidth");
      d = b.style.getComputedStyle(a, "borderRightWidth");
      e = b.style.getComputedStyle(a, "borderTopWidth");
      a = b.style.getComputedStyle(a, "borderBottomWidth");
      return new z(parseFloat(e), parseFloat(d), parseFloat(a), parseFloat(c))
    }
  };
  b.style.Dh = function(a) {
    var c = b.a.t(a), d = "";
    if(c.createTextRange) {
      d = c.body.createTextRange();
      d.moveToElementText(a);
      d = d.queryCommandValue("FontName")
    }
    if(!d) {
      d = b.style.ha(a, "fontFamily");
      if(b.userAgent.fa && b.userAgent.se) {
        d = d.replace(/ \[[^\]]*\]/, "")
      }
    }
    a = d.split(",");
    if(a.length > 1) {
      d = a[0]
    }
    return b.d.ig(d, "\"'")
  };
  b.style.Qi = /[^\d]+$/;
  b.style.sf = function(a) {
    return(a = a.match(b.style.Qi)) && a[0] || null
  };
  b.style.mg = {cm:1, "in":1, mm:1, pc:1, pt:1};
  b.style.sg = {em:1, ex:1};
  b.style.Eh = function(a) {
    var c = b.style.ha(a, "fontSize"), d = b.style.sf(c);
    if(c && "px" == d) {
      return parseInt(c, 10)
    }
    if(b.userAgent.j) {
      if(d in b.style.mg) {
        return b.style.kb(a, c, "left", "pixelLeft")
      }else {
        if(a.parentNode && a.parentNode.nodeType == 1 && d in b.style.sg) {
          a = a.parentNode;
          d = b.style.ha(a, "fontSize");
          return b.style.kb(a, c == d ? "1em" : c, "left", "pixelLeft")
        }
      }
    }
    d = b.a.La("span", {style:"visibility:hidden;position:absolute;line-height:0;padding:0;margin:0;border:0;height:1em;"});
    b.a.appendChild(a, d);
    c = d.offsetHeight;
    b.a.removeNode(d);
    return c
  };
  b.style.bj = function(a) {
    var c = {};
    b.c.forEach(a.split(/\s*;\s*/), function(d) {
      d = d.split(/\s*:\s*/);
      if(d.length == 2) {
        c[b.style.Tc(d[0].toLowerCase())] = d[1]
      }
    });
    return c
  };
  b.style.Vj = function(a) {
    var c = [];
    b.object.forEach(a, function(d, e) {
      c.push(b.style.kg(e), ":", d, ";")
    });
    return c.join("")
  };
  b.style.wj = function(a, c) {
    a.style[b.userAgent.j ? "styleFloat" : "cssFloat"] = c
  };
  b.style.Ch = function(a) {
    return a.style[b.userAgent.j ? "styleFloat" : "cssFloat"] || ""
  };
  b.userAgent.product = {};
  b.userAgent.product.bd = false;
  b.userAgent.product.$c = false;
  b.userAgent.product.dd = false;
  b.userAgent.product.Zc = false;
  b.userAgent.product.ad = false;
  b.userAgent.product.gd = false;
  b.userAgent.product.Va = b.userAgent.$b || b.userAgent.cc || b.userAgent.product.bd || b.userAgent.product.$c || b.userAgent.product.dd || b.userAgent.product.Zc || b.userAgent.product.ad || b.userAgent.product.gd;
  b.userAgent.product.Sb = function() {
    b.userAgent.product.Re = false;
    b.userAgent.product.Pe = false;
    b.userAgent.product.Ue = false;
    b.userAgent.product.Oe = false;
    b.userAgent.product.Qe = false;
    b.userAgent.product.We = false;
    var a = b.userAgent.Gc();
    if(a) {
      if(a.indexOf("Firefox") != -1) {
        b.userAgent.product.Re = true
      }else {
        if(a.indexOf("Camino") != -1) {
          b.userAgent.product.Pe = true
        }else {
          if(a.indexOf("iPhone") != -1 || a.indexOf("iPod") != -1) {
            b.userAgent.product.Ue = true
          }else {
            if(a.indexOf("Android") != -1) {
              b.userAgent.product.Oe = true
            }else {
              if(a.indexOf("Chrome") != -1) {
                b.userAgent.product.Qe = true
              }else {
                if(a.indexOf("Safari") != -1) {
                  b.userAgent.product.We = true
                }
              }
            }
          }
        }
      }
    }
  };
  b.userAgent.product.Va || b.userAgent.product.Sb();
  b.userAgent.product.fa = b.userAgent.fa;
  b.userAgent.product.j = b.userAgent.j;
  b.userAgent.product.vg = b.userAgent.product.Va ? b.userAgent.product.bd : b.userAgent.product.Re;
  b.userAgent.product.pg = b.userAgent.product.Va ? b.userAgent.product.$c : b.userAgent.product.Pe;
  b.userAgent.product.xg = b.userAgent.product.Va ? b.userAgent.product.dd : b.userAgent.product.Ue;
  b.userAgent.product.ng = b.userAgent.product.Va ? b.userAgent.product.Zc : b.userAgent.product.Oe;
  b.userAgent.product.qg = b.userAgent.product.Va ? b.userAgent.product.ad : b.userAgent.product.Qe;
  b.userAgent.product.md = b.userAgent.product.Va ? b.userAgent.product.gd : b.userAgent.product.We;
  b.window = {};
  b.window.me = 500;
  b.window.oe = 690;
  b.window.ne = "google_popup";
  b.window.open = function(a, c, d) {
    c || (c = {});
    var e = d || window;
    d = typeof a.href != "undefined" ? a.href : String(a);
    a = c.target || a.target;
    var f = [];
    for(var g in c) {
      switch(g) {
        case "width":
        ;
        case "height":
        ;
        case "top":
        ;
        case "left":
          f.push(g + "=" + c[g]);
          break;
        case "target":
        ;
        case "noreferrer":
          break;
        default:
          f.push(g + "=" + (c[g] ? 1 : 0))
      }
    }
    g = f.join(",");
    if(c.noreferrer) {
      if(c = e.open("", a, g)) {
        c.document.write('<META HTTP-EQUIV="refresh" content="0; url=' + encodeURI(d) + '">');
        c.document.close()
      }
    }else {
      c = e.open(d, a, g)
    }
    return c
  };
  b.window.dj = function(a, c) {
    c || (c = {});
    c.target = c.target || a.target || b.window.ne;
    c.width = c.width || b.window.oe;
    c.height = c.height || b.window.me;
    a = b.window.open(a, c);
    if(!a) {
      return true
    }
    a.focus();
    return false
  };
  b.f("goog", b);
  b.object.extend(b, {abstractMethod:b.Cg, addDependency:b.Dg, addSingletonGetter:b.Fg, basePath:b.Ig, bind:b.Ga, cloneObject:b.sd, DEBUG:b.Ab, Disposable:o, dispose:b.oa, exportProperty:b.jh, exportSymbol:b.f, getCssName:b.Ah, getHashCode:b.ra, getMsg:b.Nh, getObjectByName:b.tf, global:b.global, globalEval:b.hi, globalize:b.ii, identityFunction:b.pi, inherits:b.Ba, isArray:b.ia, isArrayLike:b.ba, isBoolean:b.zi, isDateLike:b.Di, isDef:b.Oa, isDefAndNotNull:b.Ei, isFunction:b.pb, isNull:b.Ji, isNumber:b.Gf, 
  isObject:b.Pa, isString:b.l, LOCALE:b.zg, mixin:b.Ui, now:b.now, nullFunction:b.db, partial:b.Pc, provide:b.hj, removeHashCode:b.kj, require:b.mj, setCssNameMapping:b.sj, Timer:F, typedef:b.he, typeOf:b.zb, useStrictRequires:b.ik});
  b.f("goog.array", b.c);
  b.object.extend(b.c, {ArrayLike:b.c.og, binaryInsert:b.c.Jg, binaryRemove:b.c.Kg, binarySearch:b.c.od, bucket:b.c.Lg, clear:b.c.clear, clone:b.c.s, compare:b.c.td, contains:b.c.contains, defaultCompare:b.c.Fb, defaultCompareEquality:b.c.Ne, equals:b.c.Ma, every:b.c.every, extend:b.c.extend, filter:b.c.filter, find:b.c.find, findIndex:b.c.Dd, findIndexRight:b.c.bf, findRight:b.c.mh, flatten:b.c.df, forEach:b.c.forEach, forEachRight:b.c.ef, indexOf:b.c.indexOf, insert:b.c.ri, insertArrayAt:b.c.si, 
  insertAt:b.c.Rd, insertBefore:b.c.insertBefore, isEmpty:b.c.z, lastIndexOf:b.c.lastIndexOf, map:b.c.map, peek:b.c.cj, reduce:b.c.reduce, reduceRight:b.c.reduceRight, remove:b.c.remove, removeAt:b.c.ub, removeDuplicates:b.c.jj, removeIf:b.c.lj, repeat:b.c.repeat, rotate:b.c.rotate, slice:b.c.slice, some:b.c.some, sort:b.c.sort, sortObjectsByKey:b.c.Jj, splice:b.c.splice, stableSort:b.c.Kj, toArray:b.c.yb});
  b.f("goog.Disposable.prototype", o.prototype);
  b.object.extend(o.prototype, {dispose:o.prototype.oa, disposeInternal:o.prototype.disposeInternal, getDisposed:o.prototype.Bh, isDisposed:o.prototype.Df});
  b.f("goog.dom", b.a);
  b.object.extend(b.a, {$:b.a.Wc, $$:b.a.Xc, $dom:b.a.Yc, appendChild:b.a.appendChild, ASSUME_QUIRKS_MODE:b.a.le, ASSUME_STANDARDS_MODE:b.a.hd, canHaveChildren:b.a.canHaveChildren, compareNodeOrder:b.a.Sg, contains:b.a.contains, createDom:b.a.La, createElement:b.a.createElement, createTextNode:b.a.createTextNode, DomHelper:n, findCommonAncestor:b.a.lh, findNode:b.a.kc, findNodes:b.a.lc, flattenElement:b.a.nc, getAncestor:b.a.Ib, getAncestorByTagNameAndClass:b.a.oc, getCompatMode:b.a.Fd, getDocument:b.a.Na, 
  getDocumentHeight:b.a.Gd, getDocumentScroll:b.a.Ya, getDocumentScrollElement:b.a.sc, getDomHelper:b.a.ma, getElement:b.a.Lb, getElementsByTagNameAndClass:b.a.Za, getFirstElementChild:b.a.tc, getFrameContentDocument:b.a.Mb, getFrameContentWindow:b.a.uc, getLastElementChild:b.a.xc, getNextElementSibling:b.a.zc, getNodeAtOffset:b.a.Oh, getNodeTextLength:b.a.Ac, getNodeTextOffset:b.a.Bc, getOuterHtml:b.a.Qh, getOwnerDocument:b.a.t, getPageScroll:b.a.Th, getPreviousElementSibling:b.a.Dc, getRawTextContent:b.a.Wh, 
  getTextContent:b.a.mb, getViewportSize:b.a.Pb, getWindow:b.a.$a, htmlToDocumentFragment:b.a.Od, insertSiblingAfter:b.a.Kc, insertSiblingBefore:b.a.Lc, isCss1CompatMode:b.a.Ca, isFocusableTabIndex:b.a.Hi, isNodeLike:b.a.Tb, isNodeList:b.a.Ff, removeChildren:b.a.Vb, removeNode:b.a.removeNode, replaceNode:b.a.Qc, setFocusableTabIndex:b.a.xj, setProperties:b.a.Wb, setTextContent:b.a.Rc});
  b.f("goog.dom.classes", b.a.k);
  b.object.extend(b.a.k, {add:b.a.k.add, addRemove:b.a.k.Eg, enable:b.a.k.Ye, get:b.a.k.qa, has:b.a.k.Nd, remove:b.a.k.remove, set:b.a.k.sa, swap:b.a.k.Oj, toggle:b.a.k.Wj});
  b.f("goog.dom.DomHelper.prototype", n.prototype);
  b.object.extend(n.prototype, {$:n.prototype.Wc, $$:n.prototype.Xc, $dom:n.prototype.Yc, appendChild:n.prototype.appendChild, contains:n.prototype.contains, createDom:n.prototype.La, createElement:n.prototype.createElement, createTextNode:n.prototype.createTextNode, findNode:n.prototype.kc, findNodes:n.prototype.lc, flattenElement:n.prototype.nc, getAncestor:n.prototype.Ib, getAncestorByTagNameAndClass:n.prototype.oc, getCompatMode:n.prototype.Fd, getDocument:n.prototype.Na, getDocumentHeight:n.prototype.Gd, 
  getDocumentScroll:n.prototype.Ya, getDocumentScrollElement:n.prototype.sc, getDomHelper:n.prototype.ma, getElement:n.prototype.Lb, getElementsByTagNameAndClass:n.prototype.Za, getFirstElementChild:n.prototype.tc, getFrameContentDocument:n.prototype.Mb, getFrameContentWindow:n.prototype.uc, getLastElementChild:n.prototype.xc, getNextElementSibling:n.prototype.zc, getNodeTextLength:n.prototype.Ac, getNodeTextOffset:n.prototype.Bc, getOwnerDocument:n.prototype.t, getPreviousElementSibling:n.prototype.Dc, 
  getTextContent:n.prototype.mb, getViewportSize:n.prototype.Pb, getWindow:n.prototype.$a, htmlToDocumentFragment:n.prototype.Od, insertSiblingAfter:n.prototype.Kc, insertSiblingBefore:n.prototype.Lc, isCss1CompatMode:n.prototype.Ca, isNodeLike:n.prototype.Tb, removeChildren:n.prototype.Vb, removeNode:n.prototype.removeNode, replaceNode:n.prototype.Qc, setDocument:n.prototype.uj, setProperties:n.prototype.Wb, setTextContent:n.prototype.Rc});
  b.f("goog.dom.xml", b.a.xml);
  b.object.extend(b.a.xml, {createDocument:b.a.xml.createDocument, loadXml:b.a.xml.Ri, MAX_ELEMENT_DEPTH:b.a.xml.te, MAX_XML_SIZE_KB:b.a.xml.ue, selectNodes:b.a.xml.selectNodes, selectSingleNode:b.a.xml.selectSingleNode, serialize:b.a.xml.wb});
  b.f("goog.events", b.b);
  b.object.extend(b.b, {BrowserEvent:s, dispatchEvent:b.b.dispatchEvent, Event:H, EventHandler:C, EventTarget:G, EventWrapper:b.b.Ta, expose:b.b.kh, fireListener:b.b.mc, fireListeners:b.b.oh, getListener:b.b.Nb, getListeners:b.b.Kh, getTotalListenerCount:b.b.di, getUniqueId:b.b.ei, hasListener:b.b.li, KeyEvent:V, KeyHandler:K, listen:b.b.ea, Listener:L, listenOnce:b.b.sb, listenWithWrapper:b.b.Xd, protectBrowserEventEntryPoint:b.b.fj, removeAll:b.b.Ia, unlisten:b.b.Ea, unlistenByKey:b.b.za, unlistenWithWrapper:b.b.je});
  b.f("goog.events.BrowserEvent.prototype", s.prototype);
  b.object.extend(s.prototype, {altKey:s.prototype.altKey, button:s.prototype.button, charCode:s.prototype.charCode, clientX:s.prototype.clientX, clientY:s.prototype.clientY, ctrlKey:s.prototype.ctrlKey, disposeInternal:s.prototype.disposeInternal, getBrowserEvent:s.prototype.jf, init:s.prototype.bb, isButton:s.prototype.Bi, keyCode:s.prototype.keyCode, metaKey:s.prototype.metaKey, offsetX:s.prototype.offsetX, offsetY:s.prototype.offsetY, preventDefault:s.prototype.preventDefault, relatedTarget:s.prototype.relatedTarget, 
  screenX:s.prototype.screenX, screenY:s.prototype.screenY, shiftKey:s.prototype.shiftKey, stopPropagation:s.prototype.stopPropagation, target:s.prototype.target});
  b.f("goog.events.Event.prototype", H.prototype);
  b.object.extend(H.prototype, {disposeInternal:H.prototype.disposeInternal, preventDefault:H.prototype.preventDefault, stopPropagation:H.prototype.stopPropagation});
  b.f("goog.events.EventHandler", C);
  b.object.extend(C, {KEY_POOL_INITIAL_COUNT:0, KEY_POOL_MAX_COUNT:100});
  b.f("goog.events.EventHandler.prototype", C.prototype);
  b.object.extend(C.prototype, {disposeInternal:C.prototype.disposeInternal, handleEvent:C.prototype.handleEvent, listen:C.prototype.ea, listenOnce:C.prototype.sb, listenWithWrapper:C.prototype.Xd, removeAll:C.prototype.Ia, unlisten:C.prototype.Ea, unlistenWithWrapper:C.prototype.je});
  b.f("goog.events.EventTarget.prototype", G.prototype);
  b.object.extend(G.prototype, {addEventListener:G.prototype.addEventListener, dispatchEvent:G.prototype.dispatchEvent, disposeInternal:G.prototype.disposeInternal, getParentEventTarget:G.prototype.Kd, removeEventListener:G.prototype.removeEventListener, setParentEventTarget:G.prototype.Cj});
  b.f("goog.events.EventWrapper.prototype", b.b.Ta.prototype);
  b.object.extend(b.b.Ta.prototype, {listen:b.b.Ta.prototype.ea, unlisten:b.b.Ta.prototype.Ea});
  b.f("goog.events.KeyCodes", na);
  b.object.extend(na, {firesKeyPressEvent:da, isCharacterKey:P, isTextModifyingKeyEvent:function(a) {
    if(a.altKey && !a.ctrlKey || a.metaKey || a.keyCode >= 112 && a.keyCode <= 123) {
      return false
    }
    switch(a.keyCode) {
      case 18:
      ;
      case 16:
      ;
      case 17:
      ;
      case 19:
      ;
      case 20:
      ;
      case 27:
      ;
      case 33:
      ;
      case 34:
      ;
      case 36:
      ;
      case 35:
      ;
      case 37:
      ;
      case 39:
      ;
      case 38:
      ;
      case 40:
      ;
      case 45:
      ;
      case 144:
      ;
      case 93:
      ;
      case 44:
        return false;
      default:
        return true
    }
  }});
  b.f("goog.events.KeyHandler.prototype", K.prototype);
  b.object.extend(K.prototype, {attach:K.prototype.De, detach:K.prototype.detach, disposeInternal:K.prototype.disposeInternal, handleEvent:K.prototype.handleEvent});
  b.f("goog.events.Listener.prototype", L.prototype);
  b.object.extend(L.prototype, {callOnce:L.prototype.gc, handleEvent:L.prototype.handleEvent, init:L.prototype.bb, key:L.prototype.wa, removed:L.prototype.eb});
  b.f("goog.functions", b.p);
  b.object.extend(b.p, {and:b.p.Hg, compose:b.p.Tg, constant:b.p.vd, error:b.p.error, FALSE:b.p.ug, identity:b.p.oi, lock:b.p.Si, or:b.p.$i, sequence:b.p.pj, TRUE:b.p.Ag});
  b.f("goog.iter", b.e);
  b.object.extend(b.e, {chain:b.e.Pg, dropWhile:b.e.gh, equals:b.e.Ma, every:b.e.every, filter:b.e.filter, forEach:b.e.forEach, Iterable:b.e.yg, Iterator:E, join:b.e.join, map:b.e.map, nextOrValue:b.e.Vi, range:b.e.ij, reduce:b.e.reduce, some:b.e.some, StopIteration:b.e.ka, takeWhile:b.e.Pj, toArray:b.e.yb, toIterator:b.e.na});
  b.f("goog.iter.Iterator.prototype", E.prototype);
  b.object.extend(E.prototype, {next:E.prototype.next});
  E.prototype.__iterator__ = E.prototype.Aa;
  b.f("goog.json", b.va);
  b.object.extend(b.va, {parse:b.va.parse, serialize:b.va.wb, Serializer:O, unsafeParse:b.va.fk});
  b.f("goog.json.Serializer.prototype", O.prototype);
  b.object.extend(O.prototype, {serialize:O.prototype.wb});
  b.f("goog.math", b.Qf);
  b.object.extend(b.Qf, {Box:z, Coordinate:q, Rect:w, Size:p});
  b.f("goog.math.Box", z);
  b.object.extend(z, {boundingBox:function() {
    for(var a = new z(arguments[0].y, arguments[0].x, arguments[0].y, arguments[0].x), c = 1;c < arguments.length;c++) {
      var d = arguments[c];
      a.top = Math.min(a.top, d.y);
      a.right = Math.max(a.right, d.x);
      a.bottom = Math.max(a.bottom, d.y);
      a.left = Math.min(a.left, d.x)
    }
    return a
  }, contains:fa, distance:function(a, c) {
    if(c.x >= a.left && c.x <= a.right) {
      if(c.y >= a.top && c.y <= a.bottom) {
        return 0
      }
      return c.y < a.top ? a.top - c.y : c.y - a.bottom
    }
    if(c.y >= a.top && c.y <= a.bottom) {
      return c.x < a.left ? a.left - c.x : c.x - a.right
    }
    return u(c, new q(c.x < a.left ? a.left : a.right, c.y < a.top ? a.top : a.bottom))
  }, equals:function(a, c) {
    if(a == c) {
      return true
    }
    if(!a || !c) {
      return false
    }
    return a.top == c.top && a.right == c.right && a.bottom == c.bottom && a.left == c.left
  }});
  b.f("goog.math.Box.prototype", z.prototype);
  b.object.extend(z.prototype, {clone:z.prototype.s, contains:z.prototype.contains, expand:z.prototype.expand, toString:z.prototype.toString});
  b.f("goog.math.Coordinate", q);
  b.object.extend(q, {difference:A, distance:u, equals:function(a, c) {
    if(a == c) {
      return true
    }
    if(!a || !c) {
      return false
    }
    return a.x == c.x && a.y == c.y
  }, squaredDistance:function(a, c) {
    var d = a.x - c.x;
    a = a.y - c.y;
    return d * d + a * a
  }, sum:function(a, c) {
    return new q(a.x + c.x, a.y + c.y)
  }});
  b.f("goog.math.Coordinate.prototype", q.prototype);
  b.object.extend(q.prototype, {clone:q.prototype.s, toString:q.prototype.toString});
  b.f("goog.math.Rect", w);
  b.object.extend(w, {boundingRect:function(a, c) {
    if(!a || !c) {
      return null
    }
    a = a.s();
    a.Ee(c);
    return a
  }, createFromBox:function(a) {
    return new w(a.left, a.top, a.right - a.left, a.bottom - a.top)
  }, difference:ia, equals:function(a, c) {
    if(a == c) {
      return true
    }
    if(!a || !c) {
      return false
    }
    return a.left == c.left && a.width == c.width && a.top == c.top && a.height == c.height
  }, intersection:ga, intersects:ha});
  b.f("goog.math.Rect.prototype", w.prototype);
  b.object.extend(w.prototype, {boundingRect:w.prototype.Ee, clone:w.prototype.s, contains:w.prototype.contains, difference:w.prototype.fh, getSize:w.prototype.Ec, intersection:w.prototype.Sd, intersects:w.prototype.vi, toBox:w.prototype.Sj, toString:w.prototype.toString});
  b.f("goog.math.Size", p);
  b.object.extend(p, {equals:function(a, c) {
    if(a == c) {
      return true
    }
    if(!a || !c) {
      return false
    }
    return a.width == c.width && a.height == c.height
  }});
  b.f("goog.math.Size.prototype", p.prototype);
  b.object.extend(p.prototype, {area:p.prototype.Ce, aspectRatio:p.prototype.nd, ceil:p.prototype.ceil, clone:p.prototype.s, fitsInside:p.prototype.ph, floor:p.prototype.floor, getLongest:p.prototype.Lh, getShortest:p.prototype.ai, isEmpty:p.prototype.z, round:p.prototype.round, scale:p.prototype.scale, scaleToFit:p.prototype.nj, toString:p.prototype.toString});
  b.f("goog.net", b.h);
  b.object.extend(b.h, {XhrIo:t, xhrMonitor:b.h.gb, XmlHttp:b.h.n});
  b.f("goog.net.ErrorCode", ra);
  b.object.extend(ra, {getDebugMessage:function(a) {
    switch(a) {
      case 0:
        return"No Error";
      case 1:
        return"Access denied to content document";
      case 2:
        return"File not found";
      case 3:
        return"Firefox silently errored";
      case 4:
        return"Application custom error";
      case 5:
        return"An exception occurred";
      case 6:
        return"Http response at 400 or 500 level";
      case 7:
        return"Request was aborted";
      case 8:
        return"Request timed out";
      case 9:
        return"The resource is not available offline";
      default:
        return"Unrecognized error code"
    }
  }});
  b.f("goog.net.XhrIo", t);
  b.object.extend(t, {cleanup:function() {
    for(;T.length;) {
      T.pop().oa()
    }
  }, CONTENT_TYPE_HEADER:"Content-Type", FORM_CONTENT_TYPE:"application/x-www-form-urlencoded;charset=utf-8", protectEntryPoints:function(a, c) {
    t.prototype.Yd = a.gj(t.prototype.Yd, c)
  }, send:function(a, c, d, e, f, g) {
    var j = new t;
    T.push(j);
    c && b.b.ea(j, "complete", c);
    b.b.ea(j, "ready", b.Pc(va, j));
    g && j.hg(g);
    j.send(a, d, e, f)
  }});
  b.f("goog.net.XhrIo.prototype", t.prototype);
  b.object.extend(t.prototype, {abort:t.prototype.abort, dispatchEvent:t.prototype.dispatchEvent, disposeInternal:t.prototype.disposeInternal, getLastError:t.prototype.Hh, getLastErrorCode:t.prototype.Ih, getLastUri:t.prototype.Jh, getReadyState:t.prototype.lb, getResponseHeader:t.prototype.getResponseHeader, getResponseJson:t.prototype.Yh, getResponseText:t.prototype.Zh, getResponseXml:t.prototype.$h, getStatus:t.prototype.Fc, getStatusText:t.prototype.yf, getTimeoutInterval:t.prototype.ci, isActive:t.prototype.wi, 
  isComplete:t.prototype.Td, isSuccess:t.prototype.Jf, send:t.prototype.send, setTimeoutInterval:t.prototype.hg});
  b.f("goog.net.XhrMonitor_", J);
  b.object.extend(J, {getKey:Z});
  b.f("goog.net.XhrMonitor_.prototype", J.prototype);
  b.object.extend(J.prototype, {isContextSafe:J.prototype.Ci, markXhrClosed:J.prototype.Of, markXhrOpen:J.prototype.Pf, popContext:J.prototype.ae, pushContext:J.prototype.be, setEnabled:J.prototype.vj});
  b.f("goog.net.XmlHttp", b.h.n);
  b.object.extend(b.h.n, {getOptions:b.h.n.vf, setFactory:b.h.n.dg});
  b.f("goog.object", b.object);
  b.object.extend(b.object, {add:b.object.add, clear:b.object.clear, clone:b.object.s, contains:b.object.contains, containsKey:b.object.Eb, containsValue:b.object.Wa, create:b.object.create, createSet:b.object.Le, every:b.object.every, extend:b.object.extend, filter:b.object.filter, findKey:b.object.cf, findValue:b.object.nh, forEach:b.object.forEach, get:b.object.qa, getAnyKey:b.object.qh, getAnyValue:b.object.rh, getCount:b.object.da, getKeys:b.object.ga, getValues:b.object.u, isEmpty:b.object.z, 
  map:b.object.map, remove:b.object.remove, set:b.object.sa, setIfUndefined:b.object.yj, some:b.object.some, transpose:b.object.fe});
  b.f("goog.string", b.d);
  b.object.extend(b.d, {buildString:b.d.Mg, canonicalizeNewlines:b.d.Fe, caseInsensitiveCompare:b.d.rd, caseInsensitiveEndsWith:b.d.Ng, caseInsensitiveStartsWith:b.d.Og, collapseWhitespace:b.d.Rg, compareVersions:b.d.hc, contains:b.d.contains, createUniqueString:b.d.Vg, endsWith:b.d.ih, escapeChar:b.d.Ze, getRandomString:b.d.Vh, hashCode:b.d.mi, htmlEscape:b.d.Rb, isAlpha:b.d.xi, isAlphaNumeric:b.d.yi, isBreakingWhitespace:b.d.Ai, isEmpty:b.d.z, isEmptySafe:b.d.Gi, isNumeric:b.d.Ki, isSpace:b.d.Li, 
  isUnicodeChar:b.d.Mi, makeSafe:b.d.Nf, newLineToBr:b.d.Tf, normalizeSpaces:b.d.Wi, normalizeWhitespace:b.d.Xi, numerateCompare:b.d.Yi, padNumber:b.d.aj, quote:b.d.quote, regExpEscape:b.d.ce, remove:b.d.remove, removeAll:b.d.Ia, removeAt:b.d.ub, repeat:b.d.repeat, startsWith:b.d.Lj, stripNewlines:b.d.Mj, stripQuotes:b.d.ig, subs:b.d.Nj, toMap:b.d.Tj, toNumber:b.d.Uj, trim:b.d.ge, trimLeft:b.d.lg, trimRight:b.d.Yj, truncate:b.d.Zj, truncateMiddle:b.d.$j, unescapeEntities:b.d.ie, urlDecode:b.d.gk, 
  urlEncode:b.d.hk, whitespaceEscape:b.d.jk});
  b.f("goog.structs", b.g);
  b.object.extend(b.g, {clear:b.g.clear, contains:b.g.contains, every:b.g.every, filter:b.g.filter, forEach:b.g.forEach, getCount:b.g.da, getKeys:b.g.ga, getValues:b.g.u, isEmpty:b.g.z, map:b.g.map, Map:v, Set:x, SimplePool:B, some:b.g.some});
  b.f("goog.structs.Map", v);
  b.object.extend(v, {defaultEquals:ja});
  b.f("goog.structs.Map.prototype", v.prototype);
  b.object.extend(v.prototype, {addAll:v.prototype.Db, clear:v.prototype.clear, clone:v.prototype.s, containsKey:v.prototype.Eb, containsValue:v.prototype.Wa, equals:v.prototype.Ma, get:v.prototype.qa, getCount:v.prototype.da, getKeyIterator:v.prototype.Gh, getKeys:v.prototype.ga, getValueIterator:v.prototype.fi, getValues:v.prototype.u, isEmpty:v.prototype.z, remove:v.prototype.remove, set:v.prototype.sa, transpose:v.prototype.fe});
  v.prototype.__iterator__ = v.prototype.Aa;
  b.f("goog.structs.Set.prototype", x.prototype);
  b.object.extend(x.prototype, {add:x.prototype.add, addAll:x.prototype.Db, clear:x.prototype.clear, clone:x.prototype.s, contains:x.prototype.contains, containsAll:x.prototype.Ug, equals:x.prototype.Ma, getCount:x.prototype.da, getValues:x.prototype.u, intersection:x.prototype.Sd, isEmpty:x.prototype.z, isSubsetOf:x.prototype.If, remove:x.prototype.remove, removeAll:x.prototype.Ia});
  x.prototype.__iterator__ = x.prototype.Aa;
  b.f("goog.structs.SimplePool.prototype", B.prototype);
  b.object.extend(B.prototype, {createObject:B.prototype.wd, disposeInternal:B.prototype.disposeInternal, disposeObject:B.prototype.zd, getObject:B.prototype.ta, releaseObject:B.prototype.xa, setCreateObjectFn:B.prototype.xb, setDisposeObjectFn:B.prototype.tj});
  b.f("goog.style", b.style);
  b.object.extend(b.style, {clearTransparentBackgroundImage:b.style.Qg, getBackgroundColor:b.style.sh, getBorderBox:b.style.Jb, getBorderBoxSize:b.style.ff, getBounds:b.style.th, getCascadedStyle:b.style.Kb, getClientLeftTop:b.style.kf, getClientPosition:b.style.rc, getClientViewportElement:b.style.lf, getComputedCursor:b.style.uh, getComputedOverflowX:b.style.vh, getComputedOverflowY:b.style.wh, getComputedPosition:b.style.mf, getComputedStyle:b.style.getComputedStyle, getComputedTextAlign:b.style.xh, 
  getComputedZIndex:b.style.yh, getContentBoxSize:b.style.zh, getFloat:b.style.Ch, getFontFamily:b.style.Dh, getFontSize:b.style.Eh, getFramedPageOffset:b.style.rf, getLengthUnits:b.style.sf, getMarginBox:b.style.Mh, getOffsetParent:b.style.Jd, getOpacity:b.style.Ph, getPaddingBox:b.style.Cc, getPageOffset:b.style.ua, getPageOffsetLeft:b.style.Rh, getPageOffsetTop:b.style.Sh, getPosition:b.style.Uh, getRelativePosition:b.style.Xh, getSize:b.style.Ec, getStyle:b.style.bi, getVisibleRectForElement:b.style.gi, 
  installStyles:b.style.ti, isElementShown:b.style.Fi, isRightToLeft:b.style.Hf, isUnselectable:b.style.Ni, parseStyleAttribute:b.style.bj, scrollIntoContainerView:b.style.oj, setBorderBoxSize:b.style.qj, setContentBoxSize:b.style.rj, setFloat:b.style.wj, setInlineBlock:b.style.zj, setOpacity:b.style.Aj, setPageOffset:b.style.Bj, setPosition:b.style.eg, setPreWrap:b.style.Dj, setSize:b.style.Ej, setStyle:b.style.Fj, setStyles:b.style.ee, setTransparentBackgroundImage:b.style.Gj, setUnselectable:b.style.Hj, 
  showElement:b.style.Ij, toCamelCase:b.style.Tc, toSelectorCase:b.style.kg, toStyleAttribute:b.style.Vj, translateRectForAnotherFrame:b.style.Xj, uninstallStyles:b.style.ck});
  b.f("goog.Timer", F);
  b.object.extend(F, {callOnce:function(a, c, d) {
    if(b.pb(a)) {
      if(d) {
        a = b.Ga(a, d)
      }
    }else {
      if(a && typeof a.handleEvent == "function") {
        a = b.Ga(a.handleEvent, a)
      }else {
        throw Error("Invalid listener argument");
      }
    }
    return c > 2147483647 ? -1 : M.setTimeout(a, c || 0)
  }, clear:function(a) {
    M.clearTimeout(a)
  }, defaultTimerObject:M, intervalScale:0.8, TICK:sa});
  b.f("goog.Timer.prototype", F.prototype);
  b.object.extend(F.prototype, {dispatchTick:F.prototype.Xe, disposeInternal:F.prototype.disposeInternal, enabled:F.prototype.enabled, getInterval:F.prototype.Fh, setInterval:F.prototype.setInterval, start:F.prototype.start, stop:F.prototype.stop});
  b.f("goog.userAgent", b.userAgent);
  b.object.extend(b.userAgent, {ASSUME_GECKO:b.userAgent.cd, ASSUME_IE:b.userAgent.$b, ASSUME_LINUX:b.userAgent.ed, ASSUME_MAC:b.userAgent.fd, ASSUME_MOBILE_WEBKIT:b.userAgent.ac, ASSUME_OPERA:b.userAgent.cc, ASSUME_WEBKIT:b.userAgent.jd, ASSUME_WINDOWS:b.userAgent.kd, ASSUME_X11:b.userAgent.ld, compare:b.userAgent.td, GECKO:b.userAgent.la, getNavigator:b.userAgent.Ob, getUserAgentString:b.userAgent.Gc, IE:b.userAgent.j, isVersion:b.userAgent.aa, LINUX:b.userAgent.se, MAC:b.userAgent.dc, MOBILE:b.userAgent.ve, 
  OPERA:b.userAgent.fa, PLATFORM:b.userAgent.ec, SAFARI:b.userAgent.md, VERSION:b.userAgent.Cb, WEBKIT:b.userAgent.ca, WINDOWS:b.userAgent.Bg, X11:b.userAgent.ze});
  b.f("goog.userAgent.jscript", b.userAgent.q);
  b.object.extend(b.userAgent.q, {ASSUME_NO_JSCRIPT:b.userAgent.q.bc, HAS_JSCRIPT:b.userAgent.q.re, isVersion:b.userAgent.q.aa, VERSION:b.userAgent.q.Cb});
  b.f("goog.userAgent.product", b.userAgent.product);
  b.object.extend(b.userAgent.product, {ANDROID:b.userAgent.product.ng, ASSUME_ANDROID:b.userAgent.product.Zc, ASSUME_CAMINO:b.userAgent.product.$c, ASSUME_CHROME:b.userAgent.product.ad, ASSUME_FIREFOX:b.userAgent.product.bd, ASSUME_IPHONE:b.userAgent.product.dd, ASSUME_SAFARI:b.userAgent.product.gd, CAMINO:b.userAgent.product.pg, CHROME:b.userAgent.product.qg, FIREFOX:b.userAgent.product.vg, IE:b.userAgent.product.j, IPHONE:b.userAgent.product.xg, OPERA:b.userAgent.product.fa, SAFARI:b.userAgent.product.md});
  b.f("goog.window", b.window);
  b.object.extend(b.window, {DEFAULT_POPUP_HEIGHT:b.window.me, DEFAULT_POPUP_TARGET:b.window.ne, DEFAULT_POPUP_WIDTH:b.window.oe, open:b.window.open, popup:b.window.dj});
  b.f("goog.dom.TagName", {A:"A", ABBR:"ABBR", ACRONYM:"ACRONYM", ADDRESS:"ADDRESS", APPLET:"APPLET", AREA:"AREA", B:"B", BASE:"BASE", BASEFONT:"BASEFONT", BDO:"BDO", BIG:"BIG", BLOCKQUOTE:"BLOCKQUOTE", BODY:"BODY", BR:"BR", BUTTON:"BUTTON", CAPTION:"CAPTION", CENTER:"CENTER", CITE:"CITE", CODE:"CODE", COL:"COL", COLGROUP:"COLGROUP", DD:"DD", DEL:"DEL", DFN:"DFN", DIR:"DIR", DIV:"DIV", DL:"DL", DT:"DT", EM:"EM", FIELDSET:"FIELDSET", FONT:"FONT", FORM:"FORM", FRAME:"FRAME", FRAMESET:"FRAMESET", H1:"H1", 
  H2:"H2", H3:"H3", H4:"H4", H5:"H5", H6:"H6", HEAD:"HEAD", HR:"HR", HTML:"HTML", I:"I", IFRAME:"IFRAME", IMG:"IMG", INPUT:"INPUT", INS:"INS", ISINDEX:"ISINDEX", KBD:"KBD", LABEL:"LABEL", LEGEND:"LEGEND", LI:"LI", LINK:"LINK", MAP:"MAP", MENU:"MENU", META:"META", NOFRAMES:"NOFRAMES", NOSCRIPT:"NOSCRIPT", OBJECT:"OBJECT", OL:"OL", OPTGROUP:"OPTGROUP", OPTION:"OPTION", P:"P", PARAM:"PARAM", PRE:"PRE", Q:"Q", S:"S", SAMP:"SAMP", SCRIPT:"SCRIPT", SELECT:"SELECT", SMALL:"SMALL", SPAN:"SPAN", STRIKE:"STRIKE", 
  STRONG:"STRONG", STYLE:"STYLE", SUB:"SUB", SUP:"SUP", TABLE:"TABLE", TBODY:"TBODY", TD:"TD", TEXTAREA:"TEXTAREA", TFOOT:"TFOOT", TH:"TH", THEAD:"THEAD", TITLE:"TITLE", TR:"TR", TT:"TT", U:"U", UL:"UL", VAR:"VAR"});
  b.f("goog.string.Unicode", {NBSP:"\u00a0"});
  b.f("goog.dom.NodeType", {ELEMENT:1, ATTRIBUTE:2, TEXT:3, CDATA_SECTION:4, ENTITY_REFERENCE:5, ENTITY:6, PROCESSING_INSTRUCTION:7, COMMENT:8, DOCUMENT:9, DOCUMENT_TYPE:10, DOCUMENT_FRAGMENT:11, NOTATION:12});
  b.f("goog.events.BrowserEvent.MouseButton", {LEFT:0, MIDDLE:1, RIGHT:2});
  b.f("goog.events.EventType", {CLICK:"click", DBLCLICK:"dblclick", MOUSEDOWN:"mousedown", MOUSEUP:"mouseup", MOUSEOVER:"mouseover", MOUSEOUT:"mouseout", MOUSEMOVE:"mousemove", SELECTSTART:"selectstart", KEYPRESS:"keypress", KEYDOWN:"keydown", KEYUP:"keyup", BLUR:"blur", FOCUS:"focus", DEACTIVATE:"deactivate", FOCUSIN:Aa, FOCUSOUT:Ba, CHANGE:"change", SELECT:"select", SUBMIT:"submit", CONTEXTMENU:"contextmenu", DRAGSTART:"dragstart", ERROR:"error", HASHCHANGE:"hashchange", HELP:"help", LOAD:"load", 
  LOSECAPTURE:"losecapture", READYSTATECHANGE:"readystatechange", RESIZE:"resize", SCROLL:"scroll", UNLOAD:"unload"});
  b.f("goog.events.KeyCodes", {MAC_ENTER:3, BACKSPACE:8, TAB:9, NUM_CENTER:12, ENTER:13, SHIFT:16, CTRL:17, ALT:18, PAUSE:19, CAPS_LOCK:20, ESC:27, SPACE:32, PAGE_UP:33, PAGE_DOWN:34, END:35, HOME:36, LEFT:37, UP:38, RIGHT:39, DOWN:40, PRINT_SCREEN:44, INSERT:45, DELETE:46, ZERO:48, ONE:49, TWO:50, THREE:51, FOUR:52, FIVE:53, SIX:54, SEVEN:55, EIGHT:56, NINE:57, QUESTION_MARK:63, A:65, B:66, C:67, D:68, E:69, F:70, G:71, H:72, I:73, J:74, K:75, L:76, M:77, N:78, O:79, P:80, Q:81, R:82, S:83, T:84, 
  U:85, V:86, W:87, X:88, Y:89, Z:90, META:91, CONTEXT_MENU:93, NUM_ZERO:96, NUM_ONE:97, NUM_TWO:98, NUM_THREE:99, NUM_FOUR:100, NUM_FIVE:101, NUM_SIX:102, NUM_SEVEN:103, NUM_EIGHT:104, NUM_NINE:105, NUM_MULTIPLY:106, NUM_PLUS:107, NUM_MINUS:109, NUM_PERIOD:110, NUM_DIVISION:111, F1:112, F2:113, F3:114, F4:115, F5:116, F6:117, F7:118, F8:119, F9:120, F10:121, F11:122, F12:123, NUMLOCK:144, SEMICOLON:186, DASH:189, EQUALS:187, COMMA:188, PERIOD:190, SLASH:191, APOSTROPHE:192, SINGLE_QUOTE:222, OPEN_SQUARE_BRACKET:219, 
  BACKSLASH:220, CLOSE_SQUARE_BRACKET:221, WIN_KEY:224, MAC_FF_META:224, WIN_IME:229});
  b.f("goog.events.KeyHandler.EventType", {KEY:"key"});
  b.f("goog.net.ErrorCode", {NO_ERROR:0, ACCESS_DENIED:1, FILE_NOT_FOUND:2, FF_SILENT_ERROR:3, CUSTOM_ERROR:4, EXCEPTION:5, HTTP_ERROR:6, ABORT:7, TIMEOUT:8, OFFLINE:9});
  b.f("goog.net.EventType", {COMPLETE:"complete", SUCCESS:"success", ERROR:"error", ABORT:"abort", READY:"ready", READY_STATE_CHANGE:"readystatechange", TIMEOUT:"timeout", INCREMENTAL_DATA:"incrementaldata"});
  b.f("goog.net.XmlHttp.OptionType", {USE_NULL_FUNCTION:aa, LOCAL_REQUEST_ERROR:$});
  b.f("goog.net.XmlHttp.ReadyState", {UNINITIALIZED:0, LOADING:1, LOADED:2, INTERACTIVE:3, COMPLETE:4})
})();var soy = soy || {};
(function() {
  var i = navigator.userAgent, k = i.indexOf("Opera") == 0;
  soy.IS_OPERA_ = k;
  soy.IS_IE_ = !k && i.indexOf("MSIE") != -1;
  soy.IS_WEBKIT_ = !k && i.indexOf("WebKit") != -1
})();
soy.StringBuilder = function(i) {
  this.buffer_ = soy.IS_IE_ ? [] : "";
  i != null && this.append.apply(this, arguments)
};
soy.StringBuilder.prototype.bufferLength_ = 0;
soy.StringBuilder.prototype.append = function(i, k) {
  if(soy.IS_IE_) {
    if(k == null) {
      this.buffer_[this.bufferLength_++] = i
    }else {
      this.buffer_.push.apply(this.buffer_, arguments);
      this.bufferLength_ = this.buffer_.length
    }
  }else {
    this.buffer_ += i;
    if(k != null) {
      for(var m = 1;m < arguments.length;m++) {
        this.buffer_ += arguments[m]
      }
    }
  }
  return this
};
soy.StringBuilder.prototype.clear = function() {
  if(soy.IS_IE_) {
    this.bufferLength_ = this.buffer_.length = 0
  }else {
    this.buffer_ = ""
  }
};
soy.StringBuilder.prototype.toString = function() {
  if(soy.IS_IE_) {
    var i = this.buffer_.join("");
    this.clear();
    i && this.append(i);
    return i
  }else {
    return this.buffer_
  }
};
soy.renderElement = function(i, k, m) {
  i.innerHTML = k(m)
};
soy.renderAsFragment = function(i, k) {
  var m = document.createElement("div");
  m.innerHTML = i(k);
  if(m.childNodes.length == 1) {
    return m.firstChild
  }else {
    for(var o = document.createDocumentFragment();m.firstChild;) {
      o.appendChild(m.firstChild)
    }
    return o
  }
};
soy.$$augmentData = function(i, k) {
  function m() {
  }
  m.prototype = i;
  var o = new m;
  for(var q in k) {
    o[q] = k[q]
  }
  return o
};
soy.$$escapeHtml = function(i) {
  i = String(i);
  if(!soy.$$EscapeHtmlRe_.ALL_SPECIAL_CHARS.test(i)) {
    return i
  }
  if(i.indexOf("&") != -1) {
    i = i.replace(soy.$$EscapeHtmlRe_.AMP, "&amp;")
  }
  if(i.indexOf("<") != -1) {
    i = i.replace(soy.$$EscapeHtmlRe_.LT, "&lt;")
  }
  if(i.indexOf(">") != -1) {
    i = i.replace(soy.$$EscapeHtmlRe_.GT, "&gt;")
  }
  if(i.indexOf('"') != -1) {
    i = i.replace(soy.$$EscapeHtmlRe_.QUOT, "&quot;")
  }
  return i
};
soy.$$EscapeHtmlRe_ = {ALL_SPECIAL_CHARS:/[&<>\"]/, AMP:/&/g, LT:/</g, GT:/>/g, QUOT:/\"/g};
soy.$$escapeJs = function(i) {
  i = String(i);
  for(var k = [], m = 0;m < i.length;m++) {
    k[m] = soy.$$escapeChar(i.charAt(m))
  }
  return k.join("")
};
soy.$$escapeChar = function(i) {
  if(i in soy.$$escapeCharJs_) {
    return soy.$$escapeCharJs_[i]
  }
  var k = i, m = i.charCodeAt(0);
  if(m > 31 && m < 127) {
    k = i
  }else {
    if(m < 256) {
      k = "\\x";
      if(m < 16 || m > 256) {
        k += "0"
      }
    }else {
      k = "\\u";
      if(m < 4096) {
        k += "0"
      }
    }
    k += m.toString(16).toUpperCase()
  }
  return soy.$$escapeCharJs_[i] = k
};
soy.$$escapeCharJs_ = {"\u0008":"\\b", "\u000c":"\\f", "\n":"\\n", "\r":"\\r", "\t":"\\t", "\u000b":"\\x0B", '"':'\\"', "'":"\\'", "\\":"\\\\"};
soy.$$escapeUri = function(i) {
  i = String(i);
  return soy.$$ENCODE_URI_REGEXP_.test(i) ? i : encodeURIComponent(i)
};
soy.$$ENCODE_URI_REGEXP_ = /^[a-zA-Z0-9\-_.!~*'()]*$/;
soy.$$insertWordBreaks = function(i, k) {
  i = String(i);
  for(var m = [], o = 0, q = false, u = false, A = 0, p = 0, n = 0, H = i.length;n < H;++n) {
    var s = i.charCodeAt(n);
    if(A >= k && s != soy.$$CharCode_.SPACE) {
      m[o++] = i.substring(p, n);
      p = n;
      m[o++] = soy.WORD_BREAK_;
      A = 0
    }
    if(q) {
      if(s == soy.$$CharCode_.GREATER_THAN) {
        q = false
      }
    }else {
      if(u) {
        switch(s) {
          case soy.$$CharCode_.SEMI_COLON:
            u = false;
            ++A;
            break;
          case soy.$$CharCode_.LESS_THAN:
            u = false;
            q = true;
            break;
          case soy.$$CharCode_.SPACE:
            u = false;
            A = 0;
            break
        }
      }else {
        switch(s) {
          case soy.$$CharCode_.LESS_THAN:
            q = true;
            break;
          case soy.$$CharCode_.AMPERSAND:
            u = true;
            break;
          case soy.$$CharCode_.SPACE:
            A = 0;
            break;
          default:
            ++A;
            break
        }
      }
    }
  }
  m[o++] = i.substring(p);
  return m.join("")
};
soy.$$CharCode_ = {SPACE:32, AMPERSAND:38, SEMI_COLON:59, LESS_THAN:60, GREATER_THAN:62};
soy.WORD_BREAK_ = soy.IS_WEBKIT_ ? "<wbr></wbr>" : soy.IS_OPERA_ ? "&shy;" : "<wbr>";
soy.$$changeNewlineToBr = function(i) {
  i = String(i);
  if(!soy.$$CHANGE_NEWLINE_TO_BR_RE_.test(i)) {
    return i
  }
  return i.replace(/(\r\n|\r|\n)/g, "<br>")
};
soy.$$CHANGE_NEWLINE_TO_BR_RE_ = /[\r\n]/;
soy.$$bidiTextDir = function(i, k) {
  i = soy.$$bidiStripHtmlIfNecessary_(i, k);
  if(!i) {
    return 0
  }
  return soy.$$bidiDetectRtlDirectionality_(i) ? -1 : 1
};
soy.$$bidiDirAttr = function(i, k, m) {
  k = soy.$$bidiTextDir(k, m);
  if(k != i) {
    return k < 0 ? "dir=rtl" : k > 0 ? "dir=ltr" : ""
  }
  return""
};
soy.$$bidiMarkAfter = function(i, k, m) {
  var o = soy.$$bidiTextDir(k, m);
  return soy.$$bidiMarkAfterKnownDir(i, o, k, m)
};
soy.$$bidiMarkAfterKnownDir = function(i, k, m, o) {
  return i > 0 && (k < 0 || soy.$$bidiIsRtlExitText_(m, o)) ? "\u200e" : i < 0 && (k > 0 || soy.$$bidiIsLtrExitText_(m, o)) ? "\u200f" : ""
};
soy.$$bidiStripHtmlIfNecessary_ = function(i, k) {
  return k ? i.replace(soy.$$BIDI_HTML_SKIP_RE_, " ") : i
};
soy.$$BIDI_HTML_SKIP_RE_ = /<[^>]*>|&[^;]+;/g;
soy.$$bidiSpanWrap = function(i, k) {
  k = String(k);
  var m = soy.$$bidiTextDir(k, true), o = soy.$$bidiMarkAfterKnownDir(i, m, k, true);
  if(m > 0 && i <= 0) {
    k = "<span dir=ltr>" + k + "</span>"
  }else {
    if(m < 0 && i >= 0) {
      k = "<span dir=rtl>" + k + "</span>"
    }
  }
  return k + o
};
soy.$$bidiUnicodeWrap = function(i, k) {
  k = String(k);
  var m = soy.$$bidiTextDir(k, true), o = soy.$$bidiMarkAfterKnownDir(i, m, k, true);
  if(m > 0 && i <= 0) {
    k = "\u202a" + k + "\u202c"
  }else {
    if(m < 0 && i >= 0) {
      k = "\u202b" + k + "\u202c"
    }
  }
  return k + o
};
soy.$$bidiLtrChars_ = "A-Za-z\u00c0-\u00d6\u00d8-\u00f6\u00f8-\u02b8\u0300-\u0590\u0800-\u1fff\u2c00-\ufb1c\ufdfe-\ufe6f\ufefd-\uffff";
soy.$$bidiNeutralChars_ = "\u0000- !-@[-`{-\u00bf\u00d7\u00f7\u02b9-\u02ff\u2000-\u2bff";
soy.$$bidiRtlChars_ = "\u0591-\u07ff\ufb1d-\ufdfd\ufe70-\ufefc";
soy.$$bidiRtlDirCheckRe_ = new RegExp("^[^" + soy.$$bidiLtrChars_ + "]*[" + soy.$$bidiRtlChars_ + "]");
soy.$$bidiNeutralDirCheckRe_ = new RegExp("^[" + soy.$$bidiNeutralChars_ + "]*$|^http://");
soy.$$bidiIsRtlText_ = function(i) {
  return soy.$$bidiRtlDirCheckRe_.test(i)
};
soy.$$bidiIsNeutralText_ = function(i) {
  return soy.$$bidiNeutralDirCheckRe_.test(i)
};
soy.$$bidiRtlDetectionThreshold_ = 0.4;
soy.$$bidiRtlWordRatio_ = function(i) {
  var k = 0, m = 0;
  i = i.split(" ");
  for(var o = 0;o < i.length;o++) {
    if(soy.$$bidiIsRtlText_(i[o])) {
      k++;
      m++
    }else {
      soy.$$bidiIsNeutralText_(i[o]) || m++
    }
  }
  return m == 0 ? 0 : k / m
};
soy.$$bidiDetectRtlDirectionality_ = function(i) {
  return soy.$$bidiRtlWordRatio_(i) > soy.$$bidiRtlDetectionThreshold_
};
soy.$$bidiLtrExitDirCheckRe_ = new RegExp("[" + soy.$$bidiLtrChars_ + "][^" + soy.$$bidiRtlChars_ + "]*$");
soy.$$bidiRtlExitDirCheckRe_ = new RegExp("[" + soy.$$bidiRtlChars_ + "][^" + soy.$$bidiLtrChars_ + "]*$");
soy.$$bidiIsLtrExitText_ = function(i, k) {
  i = soy.$$bidiStripHtmlIfNecessary_(i, k);
  return soy.$$bidiLtrExitDirCheckRe_.test(i)
};
soy.$$bidiIsRtlExitText_ = function(i, k) {
  i = soy.$$bidiStripHtmlIfNecessary_(i, k);
  return soy.$$bidiRtlExitDirCheckRe_.test(i)
};if(typeof partychapp == "undefined") {
  var partychapp = {}
}
if(typeof partychapp.templates == "undefined") {
  partychapp.templates = {}
}
partychapp.templates.scoreTable = function(i) {
  var k = '<table class="channel-table"><tr><th class="target-cell" id="target-name-header" style="cursor: pointer; cursor: hand">Target</th><th class="score-cell" id="target-score-header" style="cursor: pointer; cursor: hand">Score</th></tr>';
  if(i.targets.length == 0) {
    k += "<tr><td>No scores yet! Start ++'ing and --'ing stuff!</td></tr>"
  }else {
    for(var m = i.targets, o = m.length, q = 0;q < o;q++) {
      k += partychapp.templates.singleTarget({channelName:i.channelName, target:m[q]})
    }
  }
  k += "</table>";
  return k
};
partychapp.templates.singleTarget = function(i) {
  return'<tr><td class="target-cell"><div class="target-name" onclick="toggleTargetDetails(this, \'' + soy.$$escapeHtml(i.channelName) + "', '" + soy.$$escapeHtml(i.target.name) + "')\">" + soy.$$escapeHtml(i.target.name) + '</div></td><td class="score-cell">' + soy.$$escapeHtml(i.target.score) + "</td></tr>"
};
