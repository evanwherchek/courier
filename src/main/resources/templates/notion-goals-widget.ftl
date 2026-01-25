<table style="width: 100%; max-width: 540px; margin-top: 30px; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 2px 4px rgba(0,0,0,0.1); font-family: Arial, sans-serif; border-radius: 10px; overflow: hidden;">
    <thead>
        <tr>
            <th style="background-color: #1a3d2e; color: #ffffff; padding: 16px; text-align: left; font-size: 18px; font-weight: 600;">
                Goals
            </th>
        </tr>
    </thead>
    <tbody>
        <#list goals as goal>
        <#if goal_index == 0 || goal.category != goals[goal_index - 1].category>
        <tr>
            <td style="padding: 12px 16px 8px 16px; background-color: #f5f5f5;">
                <span style="font-weight: 700; color: #1a1a1a; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px;">${goal.category}</span>
            </td>
        </tr>
        </#if>
        <tr style="border-bottom: 1px solid #e0e0e0;">
            <td style="padding: 16px;">
                <div style="margin-bottom: 8px;">
                    <span style="font-weight: 600; color: #333; font-size: 14px;">${goal.title}</span>
                    <span style="float: right; font-size: 12px; color: #333;">${goal.current} / ${goal.total}</span>
                </div>
                <div style="width: 100%; height: 8px; background-color: #e0e0e0; border-radius: 4px; overflow: hidden;">
                    <div style="width: ${goal.progressPercentage}%; height: 100%; background-color: #16a34a; transition: width 0.3s ease;"></div>
                </div>
            </td>
        </tr>
        </#list>
    </tbody>
</table>
