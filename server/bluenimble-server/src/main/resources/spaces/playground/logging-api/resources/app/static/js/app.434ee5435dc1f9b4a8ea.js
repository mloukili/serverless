webpackJsonp([1],{0:function(t,e){},"2ilB":function(t,e){},"3dH8":function(t,e){},"7Otq":function(t,e,s){t.exports=s.p+"static/img/logo.f1525de.png"},Bqbf:function(t,e){},NHnr:function(t,e,s){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=s("7+uW"),a=s("zL8q"),o=s.n(a),r=(s("tvR6"),{render:function(){var t=this,e=t.$createElement,s=t._self._c||e;return s("el-container",[s("el-aside",{attrs:{width:"300px"}},[s("div",{staticClass:"status"},[t._v("Connected to "+t._s(t.endpoint))]),t._v(" "),s("el-table",{staticStyle:{width:"100%"},attrs:{data:t.traces,stripe:"","row-class-name":t.status}},[s("el-table-column",{attrs:{prop:"timestamp",label:"Time",width:"120"}}),t._v(" "),s("el-table-column",{attrs:{prop:"reason",label:"Reason",width:"180"}})],1)],1),t._v(" "),s("el-container",[s("el-table",{staticStyle:{width:"100%","margin-left":"20px","margin-top":"38px"},attrs:{data:t.messages,stripe:"","row-class-name":t.level,"cell-class-name":t.cellClassName,"empty-text":"No Logs available"}},[s("el-table-column",{attrs:{type:"expand"},scopedSlots:t._u([{key:"default",fn:function(e){return e.row.trace?[s("pre",[t._v(t._s(e.row.trace))])]:void 0}}])}),t._v(" "),s("el-table-column",{attrs:{prop:"timestamp",label:"Time",width:"120"}}),t._v(" "),s("el-table-column",{attrs:{prop:"level",label:"Level",width:"80"}}),t._v(" "),s("el-table-column",{attrs:{prop:"message",label:"Message",width:"600"}})],1)],1)],1)},staticRenderFns:[]});var l=s("VU/8")({data:function(){return{}},computed:{endpoint:function(){return this.$parent.endpoint},messages:function(){return this.$parent.messages},traces:function(){return this.$parent.traces}},methods:{status:function(t){var e=t.row;return t.rowIndex,e.status},level:function(t){var e=t.row;return t.rowIndex,e.level.toLowerCase()},cellClassName:function(t){var e=t.row;if(t.column,t.rowIndex,2===t.columnIndex)return e.level.toLowerCase()+"-cell"}}},r,!1,function(t){s("bE8t")},null,null).exports,i={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"login"},[n("el-row",{staticClass:"row-bg",attrs:{type:"flex",justify:"center"}},[n("el-col",{attrs:{span:6}},[n("img",{attrs:{src:s("7Otq")}})]),t._v(" "),n("el-col",{staticClass:"form",attrs:{span:12}},[n("el-form",{attrs:{"label-width":"120px"}},[n("el-form-item",{attrs:{label:"Endpoint"}},[n("el-input",{attrs:{placeholder:"https://localhost:9696"},model:{value:t.endpoint,callback:function(e){t.endpoint=e},expression:"endpoint"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"Peer ID"}},[n("el-input",{model:{value:t.peer,callback:function(e){t.peer=e},expression:"peer"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"Peer Password"}},[n("el-input",{attrs:{type:"password","auto-complete":"off"},model:{value:t.key,callback:function(e){t.key=e},expression:"key"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"Channel"}},[n("el-input",{attrs:{placeholder:"logs/your-space/your-api"},model:{value:t.channel,callback:function(e){t.channel=e},expression:"channel"}})],1),t._v(" "),n("el-form-item",[n("el-button",{on:{click:t.authenticate}},[t._v("Connect")])],1)],1)],1),t._v(" "),n("el-col",{attrs:{span:6}})],1)],1)},staticRenderFns:[]};var c=s("VU/8")({data:function(){return{endpoint:"",peer:"",key:"",channel:""}},methods:{authenticate:function(){this.$parent.newSocket(this.endpoint,this.peer,this.key,this.channel)}}},i,!1,function(t){s("2ilB")},"data-v-25f67d8e",null).exports,u={render:function(){var t=this.$createElement;return(this._self._c||t)("div")},staticRenderFns:[]};var j=s("VU/8")({data:function(){return{}}},u,!1,function(t){s("3dH8")},"data-v-0b798413",null).exports,m=s("DmT9"),p=s.n(m);n.default.component("app-dashboard",l),n.default.component("app-login",c),n.default.component("app-signup",j);var d=s("PJh5");n.default.use(s("8AgW"),{moment:d});var f={name:"App",data:function(){return{authenticated:!1,traces:[],messages:[]}},methods:{newSocket:function(t,e,s,a){var o=this;this.socket=p()(t,{query:"token="+e+":"+s}),this.socket.on("connect",function(){console.log("connected"),o.traces.unshift({status:"info",timestamp:n.default.moment().format("HH:mm:ss.SSS"),reason:"Connected"}),o.authenticated=!0,o.endpoint=t,o.socket.emit("join",{channel:a})}),this.socket.on("message",function(t){o.messages.unshift(t)}),this.socket.on("error",function(t){t.timestamp||(t.timestamp=n.default.moment().format("HH:mm:ss.SSS")),o.traces.unshift(t)}),this.socket.on("disconnect",function(){o.traces.unshift({status:"error",timestamp:n.default.moment().format("HH:mm:ss.SSS"),reason:"Disconnected"})}),this.socket.on("info",function(t){})}}},h={render:function(){var t=this.$createElement,e=this._self._c||t;return e("div",{attrs:{id:"app"}},[this.authenticated?this._e():e("app-login"),this._v(" "),this.authenticated?e("app-dashboard"):this._e(),this._v(" "),e("router-view")],1)},staticRenderFns:[]};var v=s("VU/8")(f,h,!1,function(t){s("glbW")},null,null).exports,b=s("/ocq"),g={render:function(){var t=this.$createElement;return(this._self._c||t)("div")},staticRenderFns:[]};var k=s("VU/8")({data:function(){return{}}},g,!1,function(t){s("Bqbf")},"data-v-6b712012",null).exports;n.default.use(b.a);var w=new b.a({routes:[{path:"/",name:"Main",component:k},{path:"/login",name:"Login",component:c}]});n.default.config.productionTip=!1,n.default.use(o.a),new n.default({el:"#app",router:w,components:{App:v},template:"<App/>"})},bE8t:function(t,e){},glbW:function(t,e){},tvR6:function(t,e){},uslO:function(t,e,s){var n={"./af":"3CJN","./af.js":"3CJN","./ar":"3MVc","./ar-dz":"tkWw","./ar-dz.js":"tkWw","./ar-kw":"j8cJ","./ar-kw.js":"j8cJ","./ar-ly":"wPpW","./ar-ly.js":"wPpW","./ar-ma":"dURR","./ar-ma.js":"dURR","./ar-sa":"7OnE","./ar-sa.js":"7OnE","./ar-tn":"BEem","./ar-tn.js":"BEem","./ar.js":"3MVc","./az":"eHwN","./az.js":"eHwN","./be":"3hfc","./be.js":"3hfc","./bg":"lOED","./bg.js":"lOED","./bm":"hng5","./bm.js":"hng5","./bn":"aM0x","./bn.js":"aM0x","./bo":"w2Hs","./bo.js":"w2Hs","./br":"OSsP","./br.js":"OSsP","./bs":"aqvp","./bs.js":"aqvp","./ca":"wIgY","./ca.js":"wIgY","./cs":"ssxj","./cs.js":"ssxj","./cv":"N3vo","./cv.js":"N3vo","./cy":"ZFGz","./cy.js":"ZFGz","./da":"YBA/","./da.js":"YBA/","./de":"DOkx","./de-at":"8v14","./de-at.js":"8v14","./de-ch":"Frex","./de-ch.js":"Frex","./de.js":"DOkx","./dv":"rIuo","./dv.js":"rIuo","./el":"CFqe","./el.js":"CFqe","./en-au":"Sjoy","./en-au.js":"Sjoy","./en-ca":"Tqun","./en-ca.js":"Tqun","./en-gb":"hPuz","./en-gb.js":"hPuz","./en-ie":"ALEw","./en-ie.js":"ALEw","./en-il":"QZk1","./en-il.js":"QZk1","./en-nz":"dyB6","./en-nz.js":"dyB6","./eo":"Nd3h","./eo.js":"Nd3h","./es":"LT9G","./es-do":"7MHZ","./es-do.js":"7MHZ","./es-us":"INcR","./es-us.js":"INcR","./es.js":"LT9G","./et":"XlWM","./et.js":"XlWM","./eu":"sqLM","./eu.js":"sqLM","./fa":"2pmY","./fa.js":"2pmY","./fi":"nS2h","./fi.js":"nS2h","./fo":"OVPi","./fo.js":"OVPi","./fr":"tzHd","./fr-ca":"bXQP","./fr-ca.js":"bXQP","./fr-ch":"VK9h","./fr-ch.js":"VK9h","./fr.js":"tzHd","./fy":"g7KF","./fy.js":"g7KF","./gd":"nLOz","./gd.js":"nLOz","./gl":"FuaP","./gl.js":"FuaP","./gom-latn":"+27R","./gom-latn.js":"+27R","./gu":"rtsW","./gu.js":"rtsW","./he":"Nzt2","./he.js":"Nzt2","./hi":"ETHv","./hi.js":"ETHv","./hr":"V4qH","./hr.js":"V4qH","./hu":"xne+","./hu.js":"xne+","./hy-am":"GrS7","./hy-am.js":"GrS7","./id":"yRTJ","./id.js":"yRTJ","./is":"upln","./is.js":"upln","./it":"FKXc","./it.js":"FKXc","./ja":"ORgI","./ja.js":"ORgI","./jv":"JwiF","./jv.js":"JwiF","./ka":"RnJI","./ka.js":"RnJI","./kk":"j+vx","./kk.js":"j+vx","./km":"5j66","./km.js":"5j66","./kn":"gEQe","./kn.js":"gEQe","./ko":"eBB/","./ko.js":"eBB/","./ky":"6cf8","./ky.js":"6cf8","./lb":"z3hR","./lb.js":"z3hR","./lo":"nE8X","./lo.js":"nE8X","./lt":"/6P1","./lt.js":"/6P1","./lv":"jxEH","./lv.js":"jxEH","./me":"svD2","./me.js":"svD2","./mi":"gEU3","./mi.js":"gEU3","./mk":"Ab7C","./mk.js":"Ab7C","./ml":"oo1B","./ml.js":"oo1B","./mn":"CqHt","./mn.js":"CqHt","./mr":"5vPg","./mr.js":"5vPg","./ms":"ooba","./ms-my":"G++c","./ms-my.js":"G++c","./ms.js":"ooba","./mt":"oCzW","./mt.js":"oCzW","./my":"F+2e","./my.js":"F+2e","./nb":"FlzV","./nb.js":"FlzV","./ne":"/mhn","./ne.js":"/mhn","./nl":"3K28","./nl-be":"Bp2f","./nl-be.js":"Bp2f","./nl.js":"3K28","./nn":"C7av","./nn.js":"C7av","./pa-in":"pfs9","./pa-in.js":"pfs9","./pl":"7LV+","./pl.js":"7LV+","./pt":"ZoSI","./pt-br":"AoDM","./pt-br.js":"AoDM","./pt.js":"ZoSI","./ro":"wT5f","./ro.js":"wT5f","./ru":"ulq9","./ru.js":"ulq9","./sd":"fW1y","./sd.js":"fW1y","./se":"5Omq","./se.js":"5Omq","./si":"Lgqo","./si.js":"Lgqo","./sk":"OUMt","./sk.js":"OUMt","./sl":"2s1U","./sl.js":"2s1U","./sq":"V0td","./sq.js":"V0td","./sr":"f4W3","./sr-cyrl":"c1x4","./sr-cyrl.js":"c1x4","./sr.js":"f4W3","./ss":"7Q8x","./ss.js":"7Q8x","./sv":"Fpqq","./sv.js":"Fpqq","./sw":"DSXN","./sw.js":"DSXN","./ta":"+7/x","./ta.js":"+7/x","./te":"Nlnz","./te.js":"Nlnz","./tet":"gUgh","./tet.js":"gUgh","./tg":"5SNd","./tg.js":"5SNd","./th":"XzD+","./th.js":"XzD+","./tl-ph":"3LKG","./tl-ph.js":"3LKG","./tlh":"m7yE","./tlh.js":"m7yE","./tr":"k+5o","./tr.js":"k+5o","./tzl":"iNtv","./tzl.js":"iNtv","./tzm":"FRPF","./tzm-latn":"krPU","./tzm-latn.js":"krPU","./tzm.js":"FRPF","./ug-cn":"To0v","./ug-cn.js":"To0v","./uk":"ntHu","./uk.js":"ntHu","./ur":"uSe8","./ur.js":"uSe8","./uz":"XU1s","./uz-latn":"/bsm","./uz-latn.js":"/bsm","./uz.js":"XU1s","./vi":"0X8Q","./vi.js":"0X8Q","./x-pseudo":"e/KL","./x-pseudo.js":"e/KL","./yo":"YXlc","./yo.js":"YXlc","./zh-cn":"Vz2w","./zh-cn.js":"Vz2w","./zh-hk":"ZUyn","./zh-hk.js":"ZUyn","./zh-tw":"BbgG","./zh-tw.js":"BbgG"};function a(t){return s(o(t))}function o(t){var e=n[t];if(!(e+1))throw new Error("Cannot find module '"+t+"'.");return e}a.keys=function(){return Object.keys(n)},a.resolve=o,t.exports=a,a.id="uslO"}},["NHnr"]);
//# sourceMappingURL=app.434ee5435dc1f9b4a8ea.js.map