/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated.tables.daos;


import edu.uci.ics.texera.web.model.jooq.generated.tables.UserProject;
import edu.uci.ics.texera.web.model.jooq.generated.tables.records.UserProjectRecord;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserProjectDao extends DAOImpl<UserProjectRecord, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject, UInteger> {

    /**
     * Create a new UserProjectDao without any configuration
     */
    public UserProjectDao() {
        super(UserProject.USER_PROJECT, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject.class);
    }

    /**
     * Create a new UserProjectDao with an attached configuration
     */
    public UserProjectDao(Configuration configuration) {
        super(UserProject.USER_PROJECT, edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject.class, configuration);
    }

    @Override
    public UInteger getId(edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject object) {
        return object.getPid();
    }

    /**
     * Fetch records that have <code>pid BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchRangeOfPid(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(UserProject.USER_PROJECT.PID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>pid IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchByPid(UInteger... values) {
        return fetch(UserProject.USER_PROJECT.PID, values);
    }

    /**
     * Fetch a unique record that has <code>pid = value</code>
     */
    public edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject fetchOneByPid(UInteger value) {
        return fetchOne(UserProject.USER_PROJECT.PID, value);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(UserProject.USER_PROJECT.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchByName(String... values) {
        return fetch(UserProject.USER_PROJECT.NAME, values);
    }

    /**
     * Fetch records that have <code>owner_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchRangeOfOwnerId(UInteger lowerInclusive, UInteger upperInclusive) {
        return fetchRange(UserProject.USER_PROJECT.OWNER_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>owner_id IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchByOwnerId(UInteger... values) {
        return fetch(UserProject.USER_PROJECT.OWNER_ID, values);
    }

    /**
     * Fetch records that have <code>creation_time BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchRangeOfCreationTime(Timestamp lowerInclusive, Timestamp upperInclusive) {
        return fetchRange(UserProject.USER_PROJECT.CREATION_TIME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>creation_time IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchByCreationTime(Timestamp... values) {
        return fetch(UserProject.USER_PROJECT.CREATION_TIME, values);
    }

    /**
     * Fetch records that have <code>color BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchRangeOfColor(String lowerInclusive, String upperInclusive) {
        return fetchRange(UserProject.USER_PROJECT.COLOR, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>color IN (values)</code>
     */
    public List<edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.UserProject> fetchByColor(String... values) {
        return fetch(UserProject.USER_PROJECT.COLOR, values);
    }
}
