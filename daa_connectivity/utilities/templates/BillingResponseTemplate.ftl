<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <soap:Body>
      <ns2:ougWSResponse xmlns:ns2="http://ougwebcomponent.components.oug.osgi.scorex.com/">
         <data_flux>
			<item>result</item>	
			<item>BILL_${.vars["/message/result"]}_BILL</item>
         </data_flux>
		 <data_flux>
			<item>result</item>	
			<item>TRANSID_${.vars["/message/transId"]}_TRANSID</item>
         </data_flux>
      </ns2:ougWSResponse>
   </soap:Body>
</soap:Envelope>