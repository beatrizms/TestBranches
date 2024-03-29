<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:oug="http://ougwebcomponent.components.oug.osgi.scorex.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <oug:ougWS>
         <workflow_name>Billing</workflow_name>
         <!--Zero or more repetitions:-->
         <data_flux>
            <item>initiator</item>
            <item>ut_mbt</item>
         </data_flux>
         <data_flux>
            <item>pin</item>
            <item>123456</item>
         </data_flux>
          <data_flux>
            <item>flag</item>
            <item>${flag}</item>
         </data_flux>
         <data_flux>
            <item>reseller</item>
            <item>1111111</item>
         </data_flux>
         <data_flux>
            <item>subscriber</item>
            <item>84${.vars["/message/MOBILEIDENTITYNUMBER_MIN"]}</item>
         </data_flux>
		 <data_flux>
			<item>amount</item>
			<item>${.vars["/message/amount"]}</item>
		</data_flux>
		<data_flux>
			<item>transid_request</item>
			<item>${.vars["/message/transid_request"]}</item>
		</data_flux>
      </oug:ougWS>
   </soapenv:Body>
</soapenv:Envelope>