select
    d.site_identification_number,
    d.agency_code,
    d.vertical_datum_code,
    d.measurement_source_code,
    d.measuring_agency_code,
    d.level_accuracy_code,
    d.result_measure_qualifiers::varchar,
    d.measurement_method_code,
    d.date_measured,
    d.date_measured_raw,
    d.date_time_accuracy_code,
    d.timezone_code,
    d.time_measured_utc,
    d.approval_level,
    d.parameter_code,
    d.display_result
  from nwis.discrete_ground_water_aqts d
  join nwis.nwis_district_cds_by_host h
    on d.district_cd = h.district_cd
 where h.host_name not like 'nwisd%'
   and h.state_name in (:states)
   and d.parameter_code not in ('61055')
 order by h.state_name, d.monitoring_location_identifier, d.date_measured_raw
