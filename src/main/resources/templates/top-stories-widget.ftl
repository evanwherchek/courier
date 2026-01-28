<table style="width: 100%; max-width: 540px; margin-top: 30px; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 2px 4px rgba(0,0,0,0.1); font-family: Arial, sans-serif; border-radius: 10px; overflow: hidden;">
    <thead>
        <tr>
            <th style="background-color: #1a3d2e; color: #ffffff; padding: 16px; text-align: left; font-size: 18px; font-weight: 600;">
                Top Stories
            </th>
        </tr>
    </thead>
    <tbody>
        <#list stories as story>
            <tr style="border-bottom: 1px solid #e0e0e0;">
                <td style="padding: 16px;">
                    <div style="margin-bottom: 4px;">
                        <a href="${story.link}" style="font-weight: 600; color: #1a3d2e; font-size: 15px; text-decoration: none; line-height: 1.4;">
                            ${story.title}
                        </a>
                    </div>
                    <div style="font-size: 12px; color: #333; margin-top: 4px;">
                        ${story.pubDate}
                    </div>
                </td>
            </tr>
        </#list>
        <#if stories?size == 0>
            <tr>
                <td style="padding: 16px; text-align: center; color: #333;">
                    No recent stories available
                </td>
            </tr>
        </#if>
    </tbody>
</table>
