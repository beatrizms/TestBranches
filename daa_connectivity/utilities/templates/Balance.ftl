<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:umar="http://UMARKETSCWS">
   <soapenv:Header/>
   <soapenv:Body>
      <umar:balance soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
         <paramBalance xsi:type="umar:BalanceRequestType">
            <sessionid xsi:type="xsd:string">${.vars["/message/sessionId"]}</sessionid>
            <type xsi:type="xsd:int">2</type>
         </paramBalance>
      </umar:balance>
   </soapenv:Body>
</soapenv:Envelope>
