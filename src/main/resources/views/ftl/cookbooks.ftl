<#-- @ftlvariable name="" type="net.johnewart.barista.views.CookbooksView" -->

<#include "layout.ftl">
<@layout>
    <table>
        <#list cookbooks as c>
            <tr class='${["odd", "even"][c_index%2]}'>
                <td class="query">${c.cookbookName}</td>
                <td>${c.version}</td>
                <td><a href="/cookbooks/${c.cookbookName}/${c.version}">View</a></td>
            </tr>
        </#list>
    </table>
</@layout>