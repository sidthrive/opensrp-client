package org.ei.opensrp.path.listener;

import org.ei.opensrp.path.domain.WeightWrapper;

/**
 * Created by keyman on 22/11/2016.
 */
public interface WeightActionListener {

    public void onWeightTakenToday(WeightWrapper tag);

    public void onWeightTakenEarlier(WeightWrapper tag);

}
