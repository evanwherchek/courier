<svg width="400" height="200" xmlns="http://www.w3.org/2000/svg">
    <!-- Title -->
    <text x="200" y="15" text-anchor="middle" font-size="14" font-weight="bold" fill="#333">
        ${chartTitle}
    </text>

    <!-- Background -->
    <rect x="0" y="20" width="400" height="180" fill="#f9f9f9"/>

    <!-- Grid lines -->
    <#list 0..4 as i>
        <line x1="40" y1="${40 + i * 35}" x2="380" y2="${40 + i * 35}"
              stroke="#e0e0e0" stroke-width="1"/>
    </#list>

    <!-- Y-axis -->
    <line x1="40" y1="40" x2="40" y2="180" stroke="#333" stroke-width="2"/>

    <!-- X-axis -->
    <line x1="40" y1="180" x2="380" y2="180" stroke="#333" stroke-width="2"/>

    <!-- Calculate points for the line -->
    <#assign pointsStr = "">
    <#assign xStep = 340 / (dataPoints?size - 1)>
    <#list dataPoints as point>
        <#assign x = 40 + point_index * xStep>
        <#-- Scale y value: assuming maxValue is passed in -->
        <#assign y = 180 - ((point.value / maxValue) * 140)>
        <#assign pointsStr = pointsStr + x + "," + y>
        <#if point_has_next>
            <#assign pointsStr = pointsStr + " ">
        </#if>
    </#list>

    <!-- Draw the line -->
    <polyline points="${pointsStr}"
              fill="none"
              stroke="#2196F3"
              stroke-width="2"/>

    <!-- Draw data points -->
    <#list dataPoints as point>
        <#assign x = 40 + point_index * xStep>
        <#assign y = 180 - ((point.value / maxValue) * 140)>
        <circle cx="${x}" cy="${y}" r="4" fill="#2196F3"/>
    </#list>

    <!-- X-axis labels -->
    <#list dataPoints as point>
        <#assign x = 40 + point_index * xStep>
        <text x="${x}" y="195" text-anchor="middle" font-size="10" fill="#000">
            ${point.label}
        </text>
    </#list>

    <!-- Y-axis labels -->
    <#list 0..4 as i>
        <#assign value = maxValue - (i * maxValue / 4)>
        <text x="35" y="${44 + i * 35}" text-anchor="end" font-size="10" fill="#000">
            ${value?string("0.#")}
        </text>
    </#list>
</svg>
