# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html). (Patch version X.Y.0 is implied if not specified.)

## [Unreleased](https://github.com/usgs/etl-discrete-groundwater-rdb)
-   Null fields now write an empty string instead of 'null'
-   Now mapping Aquarius Qualifier descriptions to the RDB column 'lev_status_cd'
-   Now mapping Aquarius short approval_level to NWISWeb
-   Now mapping Aquarius approval_level to NWISWeb rdb column 'lev_age_cd'
-   Remove parameter code 61055 from final RDB output

### Added
-   Initial Implementation
-   Get parameters (codes, above_datum, below_land_surface) from nwcapture db
