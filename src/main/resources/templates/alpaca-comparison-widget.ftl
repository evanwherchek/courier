<table style="width: 100%; max-width: 500px; margin-top: 50px; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 2px 4px rgba(0,0,0,0.1); font-family: Arial, sans-serif;">
    <thead>
        <tr>
            <th colspan="3" style="background-color: #1a1a1a; color: #ffffff; padding: 16px; text-align: left; font-size: 18px; font-weight: 600;">
                Markets
            </th>
        </tr>
        <tr style="background-color: #f5f5f5;">
            <th style="padding: 12px; text-align: left; font-weight: 600; color: #333; border-bottom: 2px solid #e0e0e0;">Index</th>
            <th style="padding: 12px; text-align: right; font-weight: 600; color: #333; border-bottom: 2px solid #e0e0e0;">Weekly</th>
            <th style="padding: 12px; text-align: right; font-weight: 600; color: #333; border-bottom: 2px solid #e0e0e0;">YTD</th>
        </tr>
    </thead>
    <tbody>
        <#list symbols as symbolInfo>
        <tr style="border-bottom: 1px solid #e0e0e0;">
            <td style="padding: 12px; font-weight: 500; color: #333;">${symbolInfo.symbol}</td>
            <td style="padding: 12px; text-align: right; color: <#if symbolInfo.weeklyChange?starts_with("-")>#dc2626<#else>#16a34a</#if>; font-weight: 600;">
                <#if symbolInfo.weeklyChange?starts_with("-")>▼<#else>▲</#if> ${symbolInfo.weeklyChange}
            </td>
            <td style="padding: 12px; text-align: right; color: <#if symbolInfo.ytdChange?starts_with("-")>#dc2626<#else>#16a34a</#if>; font-weight: 600;">
                <#if symbolInfo.ytdChange?starts_with("-")>▼<#else>▲</#if> ${symbolInfo.ytdChange}
            </td>
        </tr>
        </#list>
    </tbody>
</table>