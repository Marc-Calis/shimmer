/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.ihealth.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBloodPressureDataPointMapper extends IHealthDataPointMapper<BloodPressure> {

    static final double KPA_TO_MMHG_CONVERSION_RATE = 7.500617;

    @Override
    protected List<String> getListNodeNames() {
        return singletonList("BPDataList");
    }

    @Override
    protected Optional<String> getUnitPropertyNameForMeasure() { return Optional.of("BPUnit"); }

    @Override
    protected Optional<DataPoint<BloodPressure>> asDataPoint(JsonNode listNode, Integer bloodPressureUnit) {

        IHealthBloodPressureUnit bloodPressureUnitType = IHealthBloodPressureUnit.fromIntegerValue(bloodPressureUnit);

        double systolicValue = getBloodPressureValueInMmHg(asRequiredDouble(listNode, "HP"), bloodPressureUnitType);
        SystolicBloodPressure systolicBloodPressure =
                new SystolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY, systolicValue);

        double diastolicValue = getBloodPressureValueInMmHg(asRequiredDouble(listNode, "LP"), bloodPressureUnitType);
        DiastolicBloodPressure diastolicBloodPressure =
                new DiastolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY, diastolicValue);

        BloodPressure.Builder bloodPressureBuilder =
                new BloodPressure.Builder(systolicBloodPressure, diastolicBloodPressure);

        setEffectiveTimeFrameIfExists(listNode, bloodPressureBuilder);
        setUserNoteIfExists(listNode, bloodPressureBuilder);

        BloodPressure bloodPressure = bloodPressureBuilder.build();
        return Optional.of(new DataPoint<>(createDataPointHeader(listNode, bloodPressure), bloodPressure));
    }

    protected double getBloodPressureValueInMmHg(double rawBpValue, IHealthBloodPressureUnit bloodPressureUnit) {

        switch ( bloodPressureUnit ) {
            case mmHg:
                return rawBpValue;
            case KPa:
                return rawBpValue * KPA_TO_MMHG_CONVERSION_RATE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected enum IHealthBloodPressureUnit {

        mmHg(0),
        KPa(1);

        private int value;

        IHealthBloodPressureUnit(int value) {
            this.value = value;
        }

        protected int getValue() {
            return value;
        }

        public static IHealthBloodPressureUnit fromIntegerValue(int bpIntValue) {

            for (IHealthBloodPressureUnit type : values()) {
                if (type.getValue() == bpIntValue) {
                    return type;
                }
            }

            throw new UnsupportedOperationException();
        }
    }


}