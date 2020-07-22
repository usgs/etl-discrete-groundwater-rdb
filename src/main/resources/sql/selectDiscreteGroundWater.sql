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
from discrete_ground_water d
join monitoring_location m
  on m.monitoring_location_id = d.monitoring_location_id
where m.state in (:states) -- proxy for location folder
