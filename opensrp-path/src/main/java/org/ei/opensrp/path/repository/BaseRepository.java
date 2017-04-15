package org.ei.opensrp.path.repository;

/**
 * Created by keyman on 09/03/2017.
 */
public class BaseRepository {
    private PathRepository pathRepository;

    public BaseRepository(PathRepository pathRepository) {
        this.pathRepository = pathRepository;
    }

    public PathRepository getPathRepository() {
        return pathRepository;
    }
}
