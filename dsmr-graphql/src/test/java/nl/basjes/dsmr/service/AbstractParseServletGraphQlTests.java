/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2024 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nl.basjes.dsmr.service;

import io.restassured.response.ValidatableResponse;
import nl.basjes.dsmr.Version;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;

abstract class AbstractParseServletGraphQlTests extends AbstractTestingBase {

    private ValidatableResponse getGraphQLResponse(String body) {
        ValidatableResponse response =
            given()
                .port(getPort())
                .header("User-Agent", "Niels Basjes / 42")
                .accept(JSON)
                .contentType(JSON)
                .body("{\"query\":\""+ StringEscapeUtils.escapeJson(body)+"\"}")
            .when()
                .post("/graphql")
            .then();
        LOG.info("{}", response.extract().body().jsonPath().get().toString());
        return response;
    }

    // ------------------------------------------
    @Test
    void testVersion() {
        Version version = Version.getInstance();

        LOG.info("Testing GraphQL: version");
        getGraphQLResponse(
            "                                    \n" +
            "    query {                         \n" +
            "      version {                     \n" +
            "        gitCommitId                 \n" +
            "        gitCommitIdDescribeShort    \n" +
            "        buildTimeStamp              \n" +
            "        projectVersion              \n" +
            "        copyright                   \n" +
            "        license                     \n" +
            "        url                         \n" +
            "        targetJREVersion            \n" +
            "      }                             \n" +
            "    }                               \n"
            )
            .statusCode(200)
            .contentType(JSON)
            .body("data.version.gitCommitId",              equalTo(version.getGitCommitId()))
            .body("data.version.gitCommitIdDescribeShort", equalTo(version.getGitCommitIdDescribeShort()))
            .body("data.version.buildTimeStamp",           equalTo(version.getBuildTimeStamp()))
            .body("data.version.projectVersion",           equalTo(version.getProjectVersion()))
            .body("data.version.copyright",                equalTo(version.getCopyright()))
            .body("data.version.license",                  equalTo(version.getLicense()))
            .body("data.version.url",                      equalTo(version.getUrl()))
            .body("data.version.targetJREVersion",         equalTo(version.getTargetJREVersion()));
    }

//
//    @Test
//    public void testAnalyzeDirectUserAgent() {
//        LOG.info("Testing GraphQL: Query");
//        getGraphQLResponse(
//            "{" +
//            "  analyze(" +
//            "    userAgent: \""+TEST_USER_AGENT+"\", " +
//            "    requestHeaders: {" +
//            "      userAgent:\"\\\"Niels Basjes/123\\\"\", " +
//            "      secChUaArch:\"\\\"arm\\\"\", " +
//            "      secChUaBitness:\"\\\"42\\\"\"}) {" +
//            "    agentNameVersion" +
//            "    requestHeaders {      " +
//            "        userAgent      " +
//            "        secChUaArch" +
//            "      secChUaBitness    " +
//            "    }" +
//            "  }" +
//            "}")
//            .statusCode(200)
//            .contentType(JSON)
//            .body("data.analyze.agentNameVersion",   equalTo(EXPECTED_AGENTNAMEVERSION))
//            // The explicit userAgent overrides the userAgent in the RequestHeaders part
//            .body("data.analyze.requestHeaders.userAgent",      equalTo(TEST_USER_AGENT))
//            .body("data.analyze.requestHeaders.secChUaArch",    equalTo("\"arm\""))
//            .body("data.analyze.requestHeaders.secChUaBitness", equalTo("\"42\""));
//    }

//    @Test
//    public void testAnalyzeDirectField() {
//        LOG.info("Testing GraphQL: direct field");
//        getGraphQLResponse(
//            "{" +
//            "  analyze(requestHeaders: {userAgent: \""+TEST_USER_AGENT+"\"}) {" +
//            "    agentNameVersion" +
//            "  }" +
//            "}")
//            .statusCode(200)
//            .contentType(JSON)
//            .body("data.analyze.agentNameVersion",   equalTo(EXPECTED_AGENTNAMEVERSION));
//    }
//
//    @Test
//    public void testAnalyzeField() {
//        LOG.info("Testing GraphQL: single field");
//        getGraphQLResponse(
//                "{" +
//                "  analyze(requestHeaders: {userAgent: \""+TEST_USER_AGENT+"\"}) {" +
//                "    field(fieldName: \"AgentNameVersion\") {" +
//                "      fieldName" +
//                "      value" +
//                "    }" +
//                "    AgentNameVersion:field(fieldName: \"AgentNameVersion\") {" +
//                "      fieldName" +
//                "      value" +
//                "    }" +
//                "  }" +
//                "}")
//            .statusCode(200)
//            .contentType(JSON)
//            .body("data.analyze.field.fieldName",               equalTo("AgentNameVersion"))
//            .body("data.analyze.field.value",                   equalTo(EXPECTED_AGENTNAMEVERSION))
//            .body("data.analyze.AgentNameVersion.fieldName",    equalTo("AgentNameVersion"))
//            .body("data.analyze.AgentNameVersion.value",        equalTo(EXPECTED_AGENTNAMEVERSION));
//    }
//
//    @Test
//    public void testAnalyzeFields() {
//        LOG.info("Testing GraphQL: array fields");
//        getGraphQLResponse(
//            "{" +
//            "  analyze(requestHeaders: {userAgent: \""+TEST_USER_AGENT+"\"}) {" +
//            "    fields(fieldNames: [\"AgentNameVersion\"]) {" +
//            "      fieldName" +
//            "      value" +
//            "    }" +
//            "  }" +
//            "}")
//            .statusCode(200)
//            .contentType(JSON)
//            .body("data.analyze.fields[0].fieldName",        equalTo("AgentNameVersion"))
//            .body("data.analyze.fields[0].value",            equalTo(EXPECTED_AGENTNAMEVERSION));
//    }
//
//    @Test
//    public void testCheckCustomConfigWasLoaded() {
//        assumeTrue(runTestsThatNeedResourceFiles, "Some integration tests cannot use test resources");
//
//        LOG.info("Testing GraphQL: Custom resources");
//        getGraphQLResponse(
//            "{" +
//            "    analyze(requestHeaders: {" +
//            "        userAgent: \"TestApplication/1.2.3 (node123.datacenter.example.nl; 1234; d71922715c2bfe29343644b14a4731bf5690e66e)\"" +
//            "    }) {" +
//            "        ApplicationName:       field(fieldName: \"ApplicationName\")      { value }" +
//            "        ApplicationVersion:    field(fieldName: \"ApplicationVersion\")   { value }" +
//            "        ApplicationInstance:   field(fieldName: \"ApplicationInstance\")  { value }" +
//            "        ApplicationGitCommit:  field(fieldName: \"ApplicationGitCommit\") { value }" +
//            "        ServerName:            field(fieldName: \"ServerName\")           { value }" +
//            "    }" +
//            "}")
//            .statusCode(200)
//            .contentType(JSON)
//            .body("data.analyze.ApplicationName.value",       equalTo("TestApplication"))
//            .body("data.analyze.ApplicationVersion.value",    equalTo("1.2.3"))
//            .body("data.analyze.ServerName.value",            equalTo("node123.datacenter.example.nl"))
//            .body("data.analyze.ApplicationInstance.value",   equalTo("1234"))
//            .body("data.analyze.ApplicationGitCommit.value",  equalTo("d71922715c2bfe29343644b14a4731bf5690e66e"));
//    }
}
