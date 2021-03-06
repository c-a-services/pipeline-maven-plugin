/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.maven.dao;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jenkinsci.plugins.pipeline.maven.db.migration.MigrationStep;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class PipelineMavenPluginPostgreSqlDaoIT extends PipelineMavenPluginDaoAbstractTest {

    @Override
    public DataSource before_newDataSource() throws Exception {

        Class.forName("org.postgresql.Driver");

        HikariConfig config = new HikariConfig();
        String configurationFilePath = ".postgresql_config";
        InputStream propertiesInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationFilePath);

        Properties properties = new Properties();
        if (propertiesInputStream == null) {
            throw new IllegalArgumentException("Config file " + configurationFilePath + " not found in classpath");
        } else {
            try {
                properties.load(propertiesInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        config.setJdbcUrl(Preconditions.checkNotNull(properties.getProperty("jdbc.url")));
        config.setUsername(Preconditions.checkNotNull(properties.getProperty("jdbc.username")));
        config.setPassword(Preconditions.checkNotNull(properties.getProperty("jdbc.password")));
        return new HikariDataSource(config);
    }

    @Override
    public AbstractPipelineMavenPluginDao before_newAbstractPipelineMavenPluginDao(DataSource ds) {
        return new PipelineMavenPluginPostgreSqlDao(ds) {
            @Override
            protected MigrationStep.JenkinsDetails getJenkinsDetails() {
                return new MigrationStep.JenkinsDetails() {
                    @Override
                    public String getMasterLegacyInstanceId() {
                        return "123456";
                    }

                    @Override
                    public String getMasterRootUrl() {
                        return "https://jenkins.mycompany.com/";
                    }
                };
            }
        };
    }

}
