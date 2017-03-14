package org.ei.opensrp.path.listener;

import android.view.View;
import android.view.ViewGroup;

import org.ei.opensrp.path.domain.VaccineWrapper;

import java.util.List;

/**
 * Created by keyman on 22/11/2016.
 */
public interface VaccinationActionListener {

    public void onVaccinateToday(List<VaccineWrapper> tags);

    public void onVaccinateEarlier(List<VaccineWrapper> tags);

    public void onUndoVaccination(VaccineWrapper tag);
}
