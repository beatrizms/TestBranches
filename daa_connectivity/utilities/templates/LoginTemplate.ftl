<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:umar="http://UMARKETSCWS">
 <soapenv:Header/>
 <soapenv:Body>
 <umar:login soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
 <parameters xsi:type="umar:LoginRequestType">
	<sessionid xsi:type="xsd:string">${.vars["/message/sessionId"]}</sessionid> 
	 <initiator xsi:type="xsd:string">${.vars["/message/initiator"]}</initiator>	
	 <pin>${.vars["/message/loginTocken"]}</pin>
 </parameters>
 </umar:login>
 </soapenv:Body>
</soapenv:Envelope>