<?xml version="1.0"?>
<!--
	Author: Jesper Kamstrup Linnet
	Purpose: Generating an Eclipse key bindings list based on
        an export from the org.fatborn.bindings plugin
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
        <xsl:output method="html" indent="no" encoding="UTF-8"/>

        <xsl:template match="/">
                <html>
                        <head>
                                <title>Eclipse Key Bindings</title>
                                <META http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                                <style>
		      	             th {background-color:#0080C0;  color:white;}
			             tr {white-space:nowrap; }
		                </style>
                        </head>
                        <body>
                                <xsl:apply-templates/>
                        </body>
                </html>
        </xsl:template>

        <xsl:template match="keybindings">
                <table cellspacing="0" cellpadding="2" border="1">
                        <tr><th>Category</th><th>Name</th><th>When</th><th>Key Sequence</th><th>Configuration</th><th>Plugin</th><th>Locale</th><th>Platform</th></tr>
                        <xsl:apply-templates/>
                </table>
        </xsl:template>
        
        <xsl:template match="keybinding">
                <tr>
                        <td><xsl:value-of select="category"/></td>
                        <td><xsl:value-of select="scope"/></td>
                        <td><xsl:value-of select="name"/></td>
                        <td><xsl:value-of select="configuration"/></td>
                        <td><xsl:value-of select="plugin"/></td>
                        <td><xsl:value-of select="locale"/></td>
                        <td><xsl:value-of select="platform"/></td>
                        <td><xsl:value-of select="category"/></td>
                </tr>
        </xsl:template>
</xsl:stylesheet>
