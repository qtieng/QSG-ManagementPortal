package org.radarcns.management.web.rest;

import static org.radarcns.auth.authorization.Permission.SOURCE_CREATE;
import static org.radarcns.auth.authorization.Permission.SOURCE_DELETE;
import static org.radarcns.auth.authorization.Permission.SOURCE_READ;
import static org.radarcns.auth.authorization.Permission.SOURCE_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.radarcns.auth.config.Constants;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.service.SourceService;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Source.
 */
@RestController
@RequestMapping("/api")
public class SourceResource {

    private final Logger log = LoggerFactory.getLogger(SourceResource.class);

    private static final String ENTITY_NAME = "source";

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /sources : Create a new source.
     *
     * @param sourceDto the sourceDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDto, or with
     *     status 400 (Bad Request) if the source has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sources")
    @Timed
    public ResponseEntity<SourceDTO> createSource(@Valid @RequestBody SourceDTO sourceDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save Source : {}", sourceDto);
        checkPermission(getJWT(servletRequest), SOURCE_CREATE);
        if (sourceDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "A new source cannot already have an ID")).build();
        } else if (sourceDto.getSourceId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "sourceIdExists", "A new source cannot already have a Source ID")).build();
        } else if (sourceRepository.findOneBySourceName(sourceDto.getSourceName()).isPresent()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "sourceNameExists", "Source name already in use")).build();
        } else if (sourceDto.getAssigned() == null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceAssignedRequired",
                            "A new source must have the 'assigned' field specified")).body(null);
        } else {
            SourceDTO result = sourceService.save(sourceDto);
            String name = result.getSourceName();
            return ResponseEntity.created(new URI(HeaderUtil.buildPath("api", "sources", name)))
                    .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, name))
                    .body(result);
        }
    }

    /**
     * PUT  /sources : Updates an existing source.
     *
     * @param sourceDto the sourceDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceDto, or with
     *     status 400 (Bad Request) if the sourceDto is not valid, or with status 500 (Internal
     *     Server Error) if the sourceDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/sources")
    @Timed
    public ResponseEntity<SourceDTO> updateSource(@Valid @RequestBody SourceDTO sourceDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update Source : {}", sourceDto);
        if (sourceDto.getId() == null) {
            return createSource(sourceDto);
        }
        checkPermission(getJWT(servletRequest), SOURCE_UPDATE);
        SourceDTO result = sourceService.save(sourceDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, sourceDto.getSourceName()))
                .body(result);
    }

    /**
     * GET  /sources : get all the sources.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/sources")
    @Timed
    public ResponseEntity<List<SourceDTO>> getAllSources(@ApiParam Pageable pageable) {
        log.debug("REST request to get all Sources");
        Page<SourceDTO> page = sourceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/source-types");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /sources/:sourceName : get the source with this sourceName.
     *
     * @param sourceName the name of the sourceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDTO, or with status
     *     404 (Not Found)
     */
    @GetMapping("/sources/{sourceName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<SourceDTO> getSource(@PathVariable String sourceName)
            throws NotAuthorizedException {
        log.debug("REST request to get Source : {}", sourceName);
        checkPermission(getJWT(servletRequest), SOURCE_READ);
        return ResponseUtil.wrapOrNotFound(sourceService.findOneByName(sourceName));
    }

    /**
     * DELETE  /sources/:sourceName : delete the "id" source.
     *
     * @param sourceName the id of the sourceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sources/{sourceName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteSource(@PathVariable String sourceName)
            throws NotAuthorizedException {
        log.debug("REST request to delete Source : {}", sourceName);
        checkPermission(getJWT(servletRequest), SOURCE_DELETE);
        Optional<SourceDTO> sourceDto = sourceService.findOneByName(sourceName);
        if (!sourceDto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if (sourceDto.get().getAssigned()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "sourceIsAssigned", "Cannot delete an assigned source")).build();
        }
        sourceService.delete(sourceDto.get().getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME,
                sourceName)).build();
    }

}
