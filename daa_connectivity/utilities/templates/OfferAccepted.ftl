<#assign amountOffer = .vars["/message/AMOUNT_OFFER"]?number>
<#assign fees = .vars["/message/FEES"]?number>
<#assign prodId = .vars["/message/DAA_PRODUCT_ID"]>
Ban da duoc MobiFone ung ${amountOffer?string("0")}d vao TK chinh.Tien ung va phi DV ${fees?string("0")}d se duoc tru o TK chinh trong lan nap tien tiep theo.Ma GD:UT${prodId}.Tran trong cam on!