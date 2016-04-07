/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.fao.geonet.domain.statistic.SearchRequest_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Specifications for makeing queries on {@link SearchRequest} Entities.
 * <p/>
 * User: Jesse
 * Date: 10/2/13
 * Time: 7:37 AM
 */
public final class SearchRequestSpecs {
    private SearchRequestSpecs() {
        // utility classes should not have visible constructors
    }

    /**
     * Get Specification for requests that occurred more recently than or at the date.
     *
     * @param from the starting date
     * @return Specification for requests that occurred more recently than or at the date
     */
    public static Specification<SearchRequest> isMoreRecentThanOrEqualTo(final ISODate from) {
        return new Specification<SearchRequest>() {
            @Override
            public Predicate toPredicate(Root<SearchRequest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<ISODate> requestDatePath = root.get(SearchRequest_.requestDate);
                final Predicate greaterThanToDate = cb.greaterThanOrEqualTo(requestDatePath, from);
                return greaterThanToDate;
            }
        };
    }

    /**
     * Get Specification for requests that occurred before or on the date.
     *
     * @param to the starting date
     * @return Specification for requests that occurred before or on the date.
     */
    public static Specification<SearchRequest> isOlderThanOrEqualTo(final ISODate to) {
        return new Specification<SearchRequest>() {
            @Override
            public Predicate toPredicate(Root<SearchRequest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<ISODate> requestDatePath = root.get(SearchRequest_.requestDate);
                final Predicate lessThanToDate = cb.lessThanOrEqualTo(requestDatePath, to);
                return lessThanToDate;
            }
        };
    }

    /**
     * Get Specifications that are (or are not) autogenerated.
     *
     * @param autogenerated if true then select autogenerated requests
     * @return Specifications that are (or are not) autogenerated.
     */
    public static Specification<SearchRequest> isAutogenerated(final boolean autogenerated) {
        return new Specification<SearchRequest>() {
            @Override
            public Predicate toPredicate(Root<SearchRequest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Boolean> autogeneratedPath = root.get(SearchRequest_.autogenerated);
                final Predicate isAutogenerated = cb.equal(autogeneratedPath, autogenerated);
                return isAutogenerated;
            }
        };
    }

    /**
     * Create a Specification for querying for querying whether a request simple or not.
     *
     * @param simple if true then select simple requests
     * @return a Specification for querying for querying whether a request simple or not
     */
    public static Specification<SearchRequest> isSimple(final boolean simple) {
        return new Specification<SearchRequest>() {
            @Override
            public Predicate toPredicate(Root<SearchRequest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Boolean> simplePath = root.get(SearchRequest_.simple);
                final Predicate isSimple = cb.equal(simplePath, simple);
                return isSimple;
            }
        };
    }

    /**
     * Create a specification for querying by service.
     *
     * @param service the service to query for.
     * @return a specification for querying by service.
     */
    public static Specification<SearchRequest> hasService(final String service) {
        return new Specification<SearchRequest>() {
            @Override
            public Predicate toPredicate(Root<SearchRequest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<String> servicePath = root.get(SearchRequest_.service);
                final Predicate isSimple = cb.equal(servicePath, service);
                return isSimple;
            }
        };
    }

    /**
     * Create a specification for querying by number of hits.
     *
     * @param hits the number of hits.
     * @return a specification for querying by number of hits.
     */
    public static Specification<SearchRequest> hasHits(final int hits) {
        return new Specification<SearchRequest>() {
            @Override
            public Predicate toPredicate(Root<SearchRequest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Integer> hitsPath = root.get(SearchRequest_.hits);
                final Predicate isSimple = cb.equal(hitsPath, hits);
                return isSimple;
            }
        };
    }
}
