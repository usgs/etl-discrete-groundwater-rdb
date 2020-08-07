select
    d.site_identification_number,
    d.agency_code,
    d.level_feet_below_land_surface,
    d.vertical_datum_code,
    d.level_feet_above_vertical_datum,
    d.measurement_source_code,
    d.measuring_agency_code,
    d.level_accuracy_code,
    d.site_status_code,
    d.measurement_method_code,
    d.date_measured,
    d.date_measured_raw,
    d.date_time_accuracy_code,
    d.timezone_code,
    d.time_measured_utc,
    d.approval_status_code,
    d.parameter_code
  from nwis.discrete_ground_water d
  join nwis.monitoring_location m
    on m.monitoring_location_id = d.monitoring_location_id
  join nwis.nwis_district_cds_by_host h
    on m.district_cd = h.district_cd
 where h.host_name not like 'nwisd%'
   and h.state_name in (:states)
 order by h.state_name, d.monitoring_location_identifier, d.date_measured_raw
 