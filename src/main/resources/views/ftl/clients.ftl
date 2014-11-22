<#-- @ftlvariable name="" type="net.johnewart.barista.views.ClientsView" -->

<#include "layout.ftl">
<@layout>
    <table>
        <tr>
            <th>Name</th>
            <th>Validator?</th>
            <th>Admin?</th>
            <th>Action</th>
        </tr>
        <#list clients as c>
            <tr class='${["odd", "even"][c_index%2]}'>
                <td>${c.name}</td>
                <td>${c.validator?string("yes", "no")}</td>
                <td>${c.admin?string('yes', 'no')}</td>
                <td>
                    <a href="/admin/clients/${c.name}">View</a>
                    <a href="/admin/clients/${c.name}/keys">Export Keys</a>
                </td>
            </tr>
        </#list>
    </table>
</@layout>