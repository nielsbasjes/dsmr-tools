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
 */

package nl.basjes.dsmr;

public final class Version {
    private static final Version INSTANCE = new Version();

    public static Version getInstance() {
        return INSTANCE;
    }

    public static final String GIT_COMMIT_ID                = "@git.commit.id@";
    public static final String GIT_COMMIT_ID_DESCRIBE_SHORT = "@git.commit.id.describe-short@";
    public static final String BUILD_TIME_STAMP             = "@project.build.outputTimestamp@";
    public static final String PROJECT_VERSION              = "@project.version@";
    public static final String COPYRIGHT                    = "@version.copyright@";
    public static final String LICENSE                      = "@version.license@";
    public static final String URL                          = "@version.url@";
    public static final String TARGET_JRE_VERSION           = "@target.java.version@";

    public String getGitCommitId()              { return GIT_COMMIT_ID;                }
    public String getGitCommitIdDescribeShort() { return GIT_COMMIT_ID_DESCRIBE_SHORT; }
    public String getBuildTimeStamp()           { return BUILD_TIME_STAMP;             }
    public String getProjectVersion()           { return PROJECT_VERSION;              }
    public String getCopyright()                { return COPYRIGHT;                    }
    public String getLicense()                  { return LICENSE;                      }
    public String getUrl()                      { return URL;                          }
    public String getTargetJREVersion()         { return TARGET_JRE_VERSION;           }
}
