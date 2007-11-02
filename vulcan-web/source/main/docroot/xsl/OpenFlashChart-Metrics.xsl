<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:math="http://exslt.org/math">
	
	<xsl:output method="text" media-type="text/plain" version="1.0"
		encoding="UTF-8" omit-xml-declaration="yes"/>
	
	<xsl:strip-space elements="*"/>
	
	<xsl:param name="title"/>
	<xsl:param name="projectSiteURL"/>
	<xsl:param name="viewProjectStatusURL"/>
	<xsl:param name="issueTrackerURL"/>
	<xsl:param name="issueListHeader"/>
	<xsl:param name="nextBuildLabel"/>
	<xsl:param name="prevBuildLabel"/>
	<xsl:param name="sandboxLabel"/>
	<xsl:param name="buildLogLabel"/>
	<xsl:param name="revisionCaption"/>
	<xsl:param name="changeSetCaption"/>
	<xsl:param name="projectHeader"/>
	<xsl:param name="revisionHeader"/>
	<xsl:param name="buildNumberHeader"/>
	<xsl:param name="authorHeader"/>
	<xsl:param name="timestampHeader"/>
	<xsl:param name="messageHeader"/>
	<xsl:param name="pathsHeader"/>
	<xsl:param name="diffHeader"/>
	<xsl:param name="statusHeader"/>
	<xsl:param name="lastGoodBuildNumberLabel"/>
	<xsl:param name="repositoryUrlLabel"/>
	<xsl:param name="repositoryTagNameHeader"/>
	<xsl:param name="currentlyBuildingMessage"/>
	<xsl:param name="buildRequestedByLabel"/>
	<xsl:param name="buildScheduledByLabel"/>
	<xsl:param name="elapsedTimeLabel"/>
	<xsl:param name="buildReasonLabel"/>
	<xsl:param name="updateTypeLabel"/>
	<xsl:param name="warningsLabel"/>
	<xsl:param name="errorsLabel"/>
	<xsl:param name="metricsLabel"/>
	<xsl:param name="testFailureLabel"/>
	<xsl:param name="testNameLabel"/>
	<xsl:param name="testFailureBuildNumberLabel"/>
	<xsl:param name="newTestFailureLabel"/>
	
	<xsl:key name="builds-by-project-name" match="/build-history/project" use="name"/>
	<xsl:key name="builds-by-timestamp" match="/build-history/project" use="timestamp/@millis"/>
	
	<xsl:variable name="metricLabel" select="'Tests executed'"/>
	
	<xsl:template match="/build-history">
		<xsl:variable name="samplesPerLabel">
			<xsl:choose>
				<xsl:when test="count(project) &gt;= 8">
					<xsl:value-of select="floor(count(project) div 8)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="1"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="maxDuration">
			<xsl:choose>
				<xsl:when test="count(project/metrics/metric[@label=$metricLabel]/@value) != 0">
					<xsl:for-each select="project/metrics/metric[@label=$metricLabel]/@value">
						<xsl:sort data-type="number" select="." order="descending"/>
						<xsl:if test="position()=1">
							<xsl:value-of select="ceiling(. * 1.05)"/>
						</xsl:if>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="1"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:text>&amp;title=Metrics,{color: #7E97A6; font-size: 20; text-align:left}&amp;</xsl:text>

		&amp;y_min=0&amp;
		<xsl:text>&amp;y_max=</xsl:text>
		<xsl:text><xsl:value-of select="$maxDuration"/></xsl:text>
		<xsl:text>&amp;</xsl:text>

		&amp;bg_colour=#FFFFFF&amp;
		&amp;x_axis_colour=#818D9D&amp;
		&amp;x_grid_colour=#F0F0F0&amp;
		&amp;y_axis_colour=#818D9D&amp;
		&amp;y_grid_colour=#ADB5C7&amp;

		&amp;x_label_style=10,#164166,2,<xsl:value-of select="$samplesPerLabel"/>&amp;
		&amp;x_axis_steps=<xsl:value-of select="$samplesPerLabel"/>&amp;
		
		&amp;y_legend=Tests Executed,12,#164166&amp;
		&amp;y_ticks=5,10,5&amp;
		
		&amp;x_labels=<xsl:for-each select="project"><xsl:sort data-type="number" select="timestamp/@millis"/><xsl:if test="position()!=1">,</xsl:if><xsl:value-of select="substring-before(timestamp,' ')"/></xsl:for-each>&amp;
		
		<xsl:for-each select="project[generate-id() = generate-id(key('builds-by-project-name', name)[1])]">
			<xsl:variable name="name" select="name"/>
			<xsl:call-template name="dataset">
				<xsl:with-param name="seriesName" select="name"/>
				<xsl:with-param name="index" select="position()"/>
				<xsl:with-param name="color">
					<xsl:call-template name="choose-color"/>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="dataset">
		<xsl:param name="index"/>
		<xsl:param name="seriesName"/>
		<xsl:param name="color"/>
		
		<xsl:variable name="suffix"><xsl:if test="$index != 1">_<xsl:value-of select="$index"/></xsl:if></xsl:variable>
		
		&amp;line<xsl:value-of select="$suffix"/>=2,<xsl:value-of select="$color"/>,<xsl:value-of select="$seriesName"/>,12,4&amp;
		&amp;values<xsl:value-of select="$suffix"/>=<xsl:for-each select="/build-history/project">
			<xsl:sort data-type="number" select="timestamp/@millis"/>
			<xsl:if test="position()!=1">
				<xsl:text>,</xsl:text>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="name = $seriesName and metrics/metric[@label=$metricLabel]">
					<xsl:text><xsl:value-of select="metrics/metric[@label=$metricLabel]/@value"/></xsl:text>
				</xsl:when>
				<xsl:when test="name = $seriesName">
					<xsl:text>0</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>null</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>&amp;
	</xsl:template>
	
	<xsl:template name="choose-color">
		<xsl:choose>
			<xsl:when test="position() = 1">
				<xsl:text>#0066DD</xsl:text>
			</xsl:when>
			<xsl:when test="position() = 2">
				<xsl:text>#DC3912</xsl:text>
			</xsl:when>
			<xsl:when test="position() = 3">
				<xsl:text>#FF9900</xsl:text>
			</xsl:when>
			<xsl:when test="position() = 4">
				<xsl:text>#008000</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>#000000</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
