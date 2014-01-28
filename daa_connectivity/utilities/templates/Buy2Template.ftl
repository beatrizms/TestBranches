<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:umar="UMARKETSCWS">
<soapenv:Header/>
<soapenv:Body>
	<umar:buy2 soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
		<paramBuy2 xsi:type="umar:Buy2RequestType" xmlns:umar="http://UMARKETSCWS">
		<ext_couponid xsi:type="xsd:string"></ext_couponid>
		<ext_details xsi:type="xsd:string"></ext_details>
		<ext_source xsi:type="xsd:string"></ext_source>
		<ext_src_method xsi:type="xsd:string"></ext_src_method>
		<ext_usedefault xsi:type="xsd:string"></ext_usedefault>
		<schedfreq xsi:type="xsd:int"></schedfreq>
		<schedule xsi:type="xsd:string"></schedule>
		<sessionid xsi:type="xsd:string">${.vars["/message/sessionId"]}</sessionid>
		<suppress_confirm xsi:type="xsd:boolean"></suppress_confirm>
		<suppress_notify_trans xsi:type="xsd:boolean"></suppress_notify_trans>
		<wait xsi:type="xsd:boolean"></wait>
		<amount xsi:type="xsd:decimal">${.vars["/message/amount"]}</amount>
		<flag xsi:type="xsd:string">${.vars["/message/flag"]}</flag>
		<transid_request xsi:type="xsd:string">${.vars["/message/transid_request"]}</transid_request>
		<type xsi:type="xsd:int">2</type>
		<resellerMsisdn xsi:type="soapenc:string" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/">${.vars["/message/reseller"]}</resellerMsisdn>
		<subcriberMsisdn xsi:type="soapenc:string" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/">${.vars["/message/subscriber"]}</subcriberMsisdn>
		</paramBuy2>
	</umar:buy2>
</soapenv:Body>
</soapenv:Envelope>