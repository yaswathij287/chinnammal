package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Warden;
import com.smarthostel.service.WardenService;

import java.util.List;

/**
 * Controller mediating warden administration.
 */
public class WardenController {

    private final WardenService wardenService;

    public WardenController() {
        this.wardenService = new WardenService();
    }

    public WardenController(WardenService wardenService) {
        this.wardenService = wardenService;
    }

    public List<Warden> getAllWardens() throws DatabaseException {
        return wardenService.getAllWardens();
    }

    public Warden addWarden(Warden warden) throws ValidationException, DatabaseException {
        return wardenService.addWarden(warden);
    }

    public void updateWarden(Warden warden) throws ValidationException, DatabaseException {
        wardenService.updateWarden(warden);
    }

    public void deleteWarden(int wardenId) throws DatabaseException {
        wardenService.deleteWarden(wardenId);
    }

    public void assignBlock(int wardenId, Integer blockId) throws DatabaseException {
        wardenService.assignBlock(wardenId, blockId);
    }
}
