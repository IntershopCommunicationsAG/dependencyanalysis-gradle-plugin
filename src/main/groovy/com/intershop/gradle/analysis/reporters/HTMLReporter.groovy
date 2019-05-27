/*
 * Copyright 2015 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intershop.gradle.analysis.reporters

import com.intershop.gradle.analysis.model.AbstractAnalyzedDependency
import com.intershop.gradle.analysis.model.AnalyzedExternalDependency
import com.intershop.gradle.analysis.model.AnalyzedProjectDependency
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

/**
 * Implements the reporter for result output
 */
@CompileStatic
class HTMLReporter {
	
	private final Set<AbstractAnalyzedDependency> used
	private final Set<AbstractAnalyzedDependency> unused
    private final Set<AbstractAnalyzedDependency> usedTransitive
    private final Set<AbstractAnalyzedDependency> unusedTranstive
    private final Set<AbstractAnalyzedDependency> duplicates
    private final Set<AbstractAnalyzedDependency> excludedDuplicates
	private final Set<AbstractAnalyzedDependency> projectArtifacts

    /**
     * Constructs this reporter
     *
     * @param artifacts
     * @param projectArtifacts
     */
	HTMLReporter(Set<AbstractAnalyzedDependency> used, Set<AbstractAnalyzedDependency> unused, Set<AbstractAnalyzedDependency> usedTransitive,
				 Set<AbstractAnalyzedDependency> unusedTranstive, Set<AbstractAnalyzedDependency> duplicates,
				 Set<AbstractAnalyzedDependency> excludedDuplicates, Set<AbstractAnalyzedDependency>  projectArtifacts ) {
		this.used = used
        this.unused = unused
        this.usedTransitive = usedTransitive
        this.unusedTranstive = unusedTranstive
        this.duplicates = duplicates
        this.excludedDuplicates = excludedDuplicates
		this.projectArtifacts = projectArtifacts
	}

    @CompileDynamic
	void createReport(File reportFile, String projectName, String projectVersion) {
		Writer writer = new FileWriter(reportFile)
		writer.write('<!DOCTYPE html>')
		def builder = new MarkupBuilder(writer)

        int errors = duplicates.findAll({! it.ignoreForAnalysis}).size() + unused.findAll({! it.ignoreForAnalysis}).size()
        int warnings = unusedTranstive.findAll({! it.ignoreForAnalysis}).size() + usedTransitive.findAll({! it.ignoreForAnalysis}).size()

		int infos = used.findAll { it.duplicatedArtifacts.size() == 0 }.size()
		
		builder.html {
			head {
				title "Dependency Report - ${projectName}-${projectVersion}"
				style '''
                        body{margin:0;padding:0;font-family:sans-serif;font-size:12pt;}
                        body,a,a:visited{color:#303030;}
                        #content{padding-left:50px;padding-right:50px;padding-top:30px;padding-bottom:30px;}
                        #content h1{font-size:160%;margin-bottom:10px;}
                        #footer{margin-top:100px;font-size:80%;white-space:nowrap;}
                        #footer,#footer a{color:#a0a0a0;}
                        h1,h2,h3{white-space:nowrap;}
                        h2{font-size:120%;}
                        div.selected{display:block;}
                        div.deselected{display:none;}
                        #maintable{width:100%;border-collapse:collapse;}
                        #maintable th,#maintable td{border-bottom:solid #d0d0d0 1px;}
                        #maintable td{vertical-align:top}
                        th{text-align:left;white-space:nowrap;padding-left:2em;}
                        th:first-child{padding-left:0;}
                        td{padding-left:2em;padding-top:5px;padding-bottom:5px;}
                        td:first-child{padding-left:0;width:30%}
                        td.numeric,th.numeric{text-align:right;}
                        span.code{display:inline-block;margin-top:0em;margin-bottom:1em;}
                        span.code pre{font-size:11pt;padding-top:10px;padding-bottom:10px;padding-left:10px;padding-right:10px;margin:0;background-color:#f7f7f7;border:solid 1px #d0d0d0;min-width:700px;width:auto !important;width:700px;}
                        ul{margin-left:20px;padding:0px;}
                        .warning,.warning a{color:#fbcc45;}
                        .error,.error a{color:#b60808;}
                        .info, .info a{color:#3879d9}
                        #summary {margin-top: 30px;margin-bottom: 40px;border: solid 2px #d0d0d0;width:400px}
                        #summary table{border:none;}
                        #summary td{vertical-align:top;width:110px;padding-top:15px;padding-bottom:15px;text-align:center;}
                        #summary td p{margin:0;}
                       '''
			}
			body {
				div(id: "content") {
					h1 "AnalyzedExternalDependency Report - ${projectName}-${projectVersion}"
					div(id: "summary") {
						table {
							tr {
								td {
									p(class: "error", 'ERROR')
									div errors
								}
								td {
									p(class: "warning", 'WARNING')
									div warnings
								}
								td {
									p(class: "info", 'INFO')
									div infos
								}
							}
						}
					}
					h2 "Analysed Modules"
					table(id: "maintable") {
						thead {
							tr {
								th ''
								th 'AnalyzedExternalDependency Moduls'
							}
						}
						
						tbody {
                            duplicates.each { AbstractAnalyzedDependency a ->
                                tr {
                                    td { span(class: 'error', "Used, but contains duplicates") }
                                    String list = ''
                                    a.duplicatedArtifacts.each { AbstractAnalyzedDependency ad ->
                                        list += list ? ';' : ''
                                        list += "${ad.displayName}"
                                    }
                                    td {
                                        p("${a.displayName} (see also ${list})")
                                        p('Duplicate classes')
                                        ul {
                                            a.duplicatedClasses.each{ classname ->
                                                li {
                                                    mkp.yield classname
                                                }
                                            }
                                        }
                                        if(a.excludedDuplicatedClasses.size() > 0) {
                                            p('Excluded duplicate classes')
                                            ul {
                                                a.excludedDuplicatedClasses.each { classname ->
                                                    li {
                                                        mkp.yield classname
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            excludedDuplicates.each { AbstractAnalyzedDependency a ->
                                tr {
                                    td { span(class: 'info', "Used, but contains duplicates (excluded") }
                                    String list = ''
                                    a.excludedDuplicates.each { AnalyzedExternalDependency ad ->
                                        list += list ? ';' : ''
                                        list += "${ad.displayName}"
                                    }
                                    td {
                                        p("${a.identifier} (see also ${list})")
                                        p('Excluded duplicate classes')
                                        ul {
                                            a.excludedDuplicatedClasses.each{ classname ->
                                                li {
                                                    mkp.yield classname
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            unused.findAll { it.duplicatedArtifacts.size() == 0 }.each { AbstractAnalyzedDependency a ->
                                tr {
                                    if(a.ignoreForAnalysis) {
                                        td { span(class: 'info', "Not used (excluded)") }
                                    } else {
                                        td { span(class: 'error', "Not used") }
                                    }
                                    td "${a.displayName}"
                                }
                            }
                            unused.findAll { it.duplicatedArtifacts.size() > 0 }.each { AbstractAnalyzedDependency a ->
                                tr {
                                    td { span(class: 'error', "Not used (duplicate classes)") }
									String list = ''
									a.duplicatedArtifacts.each { AbstractAnalyzedDependency ad ->
										list += list ? ';' : ''
										list += "${ad.displayName}"
									}
                                    td {
										p("${a.identifier} (see also ${list})")
										p('Duplicate classes')
										ul {
											a.duplicatedClasses.each{ classname ->
												li {
													mkp.yield classname
												}
											}
										}
									}
                                }
                            }

                            usedTransitive.each { AbstractAnalyzedDependency a ->
								tr {
                                    if(a.ignoreForAnalysis) {
                                        td { span(class: 'info', "Used, but from transitive dependencies (excluded)") }
                                    } else {
                                        td { span(class: 'warning', "Used, but from transitive dependencies") }
                                    }
									td "${a.displayName}"
								}
							}
                            unusedTranstive.each { AbstractAnalyzedDependency a ->
                                tr {
                                    td { span(class: 'warning', "Not used (transitive)") }
                                    td "${a.displayName}"
                                }
                            }

							used.findAll { it.duplicatedArtifacts.size() == 0 }.each { AbstractAnalyzedDependency a ->
								tr {
									td {
										span(class: 'info', "Used for '${a.configuration}'")
									}
									td {
										mkp.yield "${a.displayName}"
										ul {
											a.usedClasses.each{ classname ->
												li {
													mkp.yield classname
												}
											}
										}
									}
								}
							}
						}
					}

					h2 "Dependency Classes"
					table(id: "maintable") {
						thead {
							tr {
								th 'Dependencies'
								th 'Dependency classes / Project classes'
							}
						}

						tbody {
							used.findAll{ it.duplicatedArtifacts.size() == 0 }.each { AbstractAnalyzedDependency a ->
								tr {
									td "${a.displayName}"
									td {
										ul {
											a.usedClasses.each {String classname ->
												li {
													mkp.yield classname
													ul {
														projectArtifacts.each { AbstractAnalyzedDependency pa ->
															pa.getDependencyMap().each { prjClass, depSet ->
																depSet.findAll { it == classname }.each {
																	li prjClass
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
                            used.findAll{ it.duplicatedArtifacts.size() > 0 }.each { AbstractAnalyzedDependency a ->
                                tr {
                                    td { span(class: 'error', "${a.module}:${a.version}") }
                                    td {
                                        ul {
                                            a.usedClasses.each {String classname ->
                                                li {
                                                    mkp.yield classname
                                                    ul {
                                                        projectArtifacts.each { AbstractAnalyzedDependency pa ->
                                                            pa.getDependencyMap().each { prjClass, depSet ->
                                                                depSet.findAll { it == classname }.each {
                                                                    li prjClass
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
						}
					}
					def props = getManifestProperties()
					p(id: "footer") {
						mkp.yield "created ${new Date()}"
						if(props && props['Implementation-Title'] && props['Implementation-Version'] && props['Implementation-Vendor'] && props['Version-Of-OW2ASM']) {
							mkp.yield " by ${props['Implementation-Title']} (${props['Implementation-Version']}) of ${props['Implementation-Vendor']} (OW2 ASM version: ${props['Version-Of-OW2ASM']})"
						}
					}
				}
			}
			
		}
		writer.flush()
	}

    /**
     *
     * @return manifest properties
     */
	private Properties getManifestProperties() {
		String resource = "/" + this.getClass().getName().replace(".", "/") + ".class"
		String fullPath = this.getClass().getResource(resource).toString()
		String archivePath = fullPath.substring(0, fullPath.length() - resource.length())
		
		try {
			InputStream is = new URL(archivePath + "/META-INF/MANIFEST.MF").openStream()
			if (is != null) {
				Properties properties = new Properties()
				properties.load(is)
				return properties
			}
		}
		catch (IOException e) {}
		return null
	}
}
