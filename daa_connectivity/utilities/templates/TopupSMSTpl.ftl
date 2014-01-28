<#assign reimbursementAmount = .vars["/message/ReimbursementAmount"]?number>
<#assign newBalance = .vars["/message/NEW_BALANCE"]?number>
<#assign prodId = .vars["/message/DAA_PRODUCT_ID"]>
<#assign topUpId = .vars["/message/TOPUP_NOTIFICATION_ID"]>
<#if .vars["/message/ProcesstreatmentTreeResults2TreatmentName"] == 'Partial Repayment'>
MobiFone da tru ${reimbursementAmount?string(",##0")}d tien ung va phi DV vao TK chinh cua ban cho GD ung tien UT${prodId}.${newBalance?string(",##0")}d con thieu se duoc tru vao lan nap tiep theo.Ma GD:HU${topUpId}.
<#else>
MobiFone da tru ${reimbursementAmount?string(",##0")}d tien ung va phi DV trong tai khoan cua ban cho GD ung tien UT${prodId} tu TK chinh cua quy khach. Ma GD:HU${topUpId}.
</#if>
