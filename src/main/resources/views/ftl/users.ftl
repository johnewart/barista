<#-- @ftlvariable name="" type="net.johnewart.barista.views.UsersView" -->

<#include "layout.ftl">
<@layout>
    <table>
        <tr>
            <th>Name</th>
            <th>Username</th>
            <th>Email</th>
            <th>Admin?</th>
            <th>Action</th>
        </tr>
        <#list users as u>
            <tr class='${["odd", "even"][u_index%2]}'>
                <td>${u.name}</td>
                <td>${u.username!""}</td>
                <td>${u.email!""}</td>
                <td>${u.admin?string('yes', 'no')}</td>
                <td>
                    <a href="/admin/users/${u.name}">View</a>
                    <a href="/admin/users/${u.name}/keys">Export Keys</a>
                </td>
            </tr>
        </#list>
    </table>
</@layout>