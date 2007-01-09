<?xml version="1.0" encoding="UTF-8" ?>

<html
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
	xmlns:html="http://jakarta.apache.org/struts/tags-html"
	xmlns:bean="http://jakarta.apache.org/struts/tags-bean"
	xmlns:v="http://vulcan.sourceforge.net/j2ee/jsp/tags">

<html:xhtml/>

<body>

<html:messages property="org.apache.struts.action.GLOBAL_MESSAGE" id="msg">
	<span class="error">${msg}</span>
</html:messages>
	
<v:bubble>
<html:form action="/admin/setup/managePlugin" method="post">
<table class="dynamicEdit">
	<caption><c:out value="${location}" escapeXml="true"/></caption>
	<tbody>
		<c:forEach items="${pluginConfigForm.allProperties}" var="desc">
			<tr>
				<jsp:element name="td">
					<jsp:attribute name="title">${desc.shortDescription}</jsp:attribute>
					<jsp:body>${desc.displayName}</jsp:body>
				</jsp:element>
				<td>
					<c:choose>
						<c:when test="${pluginConfigForm.types[desc.name] == 'primitive'}">
							<c:catch var="jspe">
								<bean:define id="value" name="pluginConfigForm"
									property="${desc.name}"/>
							</c:catch>
							<c:if test="${value == null}">
								<c:set var="value" value=""/>
							</c:if>						
							<jsp:element name="input">
								<jsp:attribute name="type">text</jsp:attribute>
								<jsp:attribute name="name">${desc.name}</jsp:attribute>
								<jsp:attribute name="value"><c:out value="${value}" escapeXml="true"/></jsp:attribute>
							</jsp:element>
						</c:when>
						<c:when test="${pluginConfigForm.types[desc.name] == 'password'}">
							<html:password property="${desc.name}"/>
						</c:when>
						<c:when test="${pluginConfigForm.types[desc.name] == 'boolean'}">
							<html:checkbox property="${desc.name}"/>
						</c:when>
						<c:when test="${pluginConfigForm.types[desc.name] == 'enum'}">
							<c:forEach items="${pluginConfigForm.choices[desc.name]}" var="choice">
								<ul class="metaDataOptions">
									<li>
										<html:radio property="${desc.name}" value="${choice}"
											styleId="radio.${desc.name}.${choice}"/>
										<jsp:element name="label">
											<jsp:attribute name="for">radio.${desc.name}.${choice}</jsp:attribute>
											<jsp:body>${choice}</jsp:body>
										</jsp:element>
									</li>
								</ul>
							</c:forEach>
						</c:when>
						<c:when test="${pluginConfigForm.types[desc.name] == 'object'}">
							<html:submit property="action" value="Configure"
								onclick="drillDown(this, '${desc.name}')"/>
						</c:when>
						<c:when test="${pluginConfigForm.types[desc.name] == 'object-array'}">
							<bean:define id="array" name="pluginConfigForm" property="${desc.name}"/>
							<table class="objectArray">
								<tbody>
									<c:set var="i" value="0"/>
									<c:forEach items="${array}" var="obj">
										<tr>
											<td><c:out value="${obj}" escapeXml="true"/></td>
											<td>
												<html:submit property="action" value="Configure"
													onclick="drillDown(this, '${desc.name}[${i}]')"/>
											</td>
											<td>
												<html:submit property="action" value="Remove"
													onclick="targetObject(this, '${desc.name}[${i}]')"/>
											</td>
										</tr>
										<c:set var="i" value="${i + 1}"/>
									</c:forEach>
								</tbody>
							</table>
							<html:submit property="action" value="Add"
								onclick="targetObject(this, '${desc.name}')"/>
						</c:when>
						<c:when test="${pluginConfigForm.types[desc.name] == 'primitive-array'}">
							<bean:define id="array" name="pluginConfigForm" property="${desc.name}"/>
							<table class="primitiveArray">
								<tbody>
									<c:set var="i" value="0"/>
									<c:forEach items="${array}" var="obj">
										<tr>
											<td>
												<jsp:element name="input">
													<jsp:attribute name="type">checkbox</jsp:attribute>
													<jsp:attribute name="name">select-${desc.name}</jsp:attribute>
												</jsp:element>
											</td>
											<td>
												<bean:define id="value" name="pluginConfigForm"
													property="${desc.name}[${i}]"/>
												<jsp:element name="input">
													<jsp:attribute name="type">text</jsp:attribute>
													<jsp:attribute name="name">${desc.name}</jsp:attribute>
													<jsp:attribute name="value"><c:out value="${value}" escapeXml="true"/></jsp:attribute>
												</jsp:element>
											</td>
										</tr>
										<c:set var="i" value="${i + 1}"/>
									</c:forEach>
								</tbody>
							</table>
							<jsp:element name="input">
								<jsp:attribute name="type">button</jsp:attribute>
								<jsp:attribute name="value">Add</jsp:attribute>
								<jsp:attribute name="name">button-${desc.name}</jsp:attribute>
							</jsp:element>
							<jsp:element name="input">
								<jsp:attribute name="type">button</jsp:attribute>
								<jsp:attribute name="value">Move Up</jsp:attribute>
								<jsp:attribute name="name">button-${desc.name}</jsp:attribute>
							</jsp:element>
							<jsp:element name="input">
								<jsp:attribute name="type">button</jsp:attribute>
								<jsp:attribute name="value">Move Down</jsp:attribute>
								<jsp:attribute name="name">button-${desc.name}</jsp:attribute>
							</jsp:element>
							<jsp:element name="input">
								<jsp:attribute name="type">button</jsp:attribute>
								<jsp:attribute name="value">Remove</jsp:attribute>
								<jsp:attribute name="name">button-${desc.name}</jsp:attribute>
							</jsp:element>
						</c:when>
						<c:when test="${pluginConfigForm.types[desc.name] == 'choice-array'}">
							<c:forEach items="${pluginConfigForm.choices[desc.name]}" var="choice">
								<ul class="metaDataOptions">
									<li>
										<html:multibox property="${desc.name}" value="${choice}"
											styleId="radio.${desc.name}.${choice}"/>
										<jsp:element name="label">
											<jsp:attribute name="for">radio.${desc.name}.${choice}</jsp:attribute>
											<jsp:body>${choice}</jsp:body>
										</jsp:element>
									</li>
								</ul>
							</c:forEach>
						</c:when>
					</c:choose>
					<html:messages property="${desc.name}" id="msg">
						<span class="error">${msg}</span>
					</html:messages>
				</td>
			</tr>
		</c:forEach>
		<tr>
			<c:set var="action" value="Update"/>
			<c:if test="${pluginConfigForm.nested or pluginConfigForm.projectPlugin}">
				<c:set var="action" value="Back"/>
			</c:if>
			<td class="buttons" colspan="2">
				<html:submit property="action" value="${action}"/>
				<html:hidden property="pluginId"/>
				<html:hidden property="projectPlugin"/>
				<html:hidden property="focus"/>
				<html:hidden property="target"/>
			</td>
		</tr>
	</tbody>
</table>
</html:form>
</v:bubble>
</body>
</html>
