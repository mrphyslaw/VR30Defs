# VR30 Tuner Calibration Guide

Use this guide to understand the VR30 controls in the tuner definition set and how an adjustment changes the ECU calculation. The set concentrates on mixture, boost, ignition, torque, cam timing, sensor calibration, protection and cooling. It is specific to this ROM family.

## Match the definition to the ROM

Choose the XML whose filename matches the calibration ID, such as `6HM8C.xml`. Match the calibration ID and full factory-file SHA256 in `candidate_index.json`; the filename alone is not proof of compatibility. A read exported with a different length or address range needs its own mapping confirmation.

These remain candidate definitions. An exact file match establishes the intended calibration mapping; it does not establish how a change behaves on a particular vehicle. Retain the original file as the comparison baseline.

## Read the values correctly

| Display | Interpretation |
| --- | --- |
| AFR | Fixed `14.7 / equivalence ratio`. Lower AFR requests richer operation. |
| Equivalence ratio | Higher values request richer operation. The direction is opposite to the AFR view. |
| Rail pressure | MPa, bar and psi are alternate views of the same stored target. `1 MPa = 10 bar`. |
| Desired boost | The inherited gauge display subtracts a fixed one bar. It does not subtract current barometric pressure. |
| Normalized load | An ECU load coordinate, not accelerator position or a universal physical air-mass measurement. |
| Cam target angle | A numerical angle coordinate. Its increase has not been confirmed as physical advance or retard. |
| Cam step limit | A step per control update. The update period is not established as a time rate. |
| MAF percent | A normalized sensor-transfer scale, not airflow in grams per second. |
| Temperature hysteresis | A temperature difference. One Celsius degree of difference is 1.8 Fahrenheit degrees, with no 32-degree offset. |
| Torque | An ECU model value or limit. A larger model value is not a promise of more delivered torque. |

Use the axes shown in the selected table. Ignition and AFR load views use `stored value * 100`; cam-target load views use `stored value * 200`, while some torque models display the ratio directly. Equal displayed percentages across those families must not be treated as proof of equal underlying coordinates. Existing numeric conversions are preserved; map-specific qualifications remain with the controls they affect.

## Work with related controls

Compare the original calibration and logged requests with measured results. Change a related control group deliberately, then check which request or limit is active before attributing a result to an edited cell. An inactive schedule can hide an edit, and a model correction can change arbitration without directly requesting more output.

### Mixture and rail pressure

The normal and safe-mode AFR maps are separate schedules. A lower AFR number requests a richer mixture, while a higher equivalence ratio requests richer operation. The fixed 14.7 display is gasoline-equivalent and does not automatically become the fuel's actual stoichiometric AFR. Do not assume a named fault selects the safe-mode schedule; its full entry conditions remain unresolved.

Rail-pressure targets are requests. The normal and low-speed schedules, lower floor and pressure controller work together. Compare requested and measured pressure; an increased target can remain unmet or be replaced by another selected schedule.

### Boost request and feedback

Desired Boost and its paired curve supply requests. The paired channel's physical identity remains unresolved, so preserve their relationship when comparing edits. Their gauge views use a fixed one-bar offset; interpretation against measured pressure still needs confirmation.

The wastegate feedback maps and integral/derivative multipliers shape corrections. Signed contributions mean that a larger correction or gain does not automatically produce more boost. The inherited wastegate millimetre and duty displays do not establish physical actuator direction. Keep those distinctions separate from a pressure-target change.

The upper/lower load-reference curves and atmospheric multiplier form another correction stage. Keep the upper reference above the lower one and review the three together. A multiplier of 1 is neutral at that stage; later scaling and caps can still change the result.

### Ignition timing and torque models

Base timing is a starting value. The maximum limit, minimum-timing contributions, coolant blend and later corrections can all affect final timing. The maximum-limit value 127 is a source no-limit marker, not an advance request. Mode 2 and Mode 3 contribute to the normal minimum calculation. The alternate Mode 1 and Mode 4 outputs have an unresolved downstream role and must not be treated as universal knock-retard limits.

Driver demand requests drive force. The load/RPM torque table and coolant multiplier change model values that other control calculations use. Increasing a model value can change arbitration rather than directly increasing engine output.

### Cam targets and response limits

Selector numbers identify ECU schedule states. They are not confirmed gears, driving modes or physical banks. Only schedules selected or blended by the ECU contribute to the request. A higher numerical cam target has not been assigned a physical advance/retard direction.

The two response-limit branches are selected by the request's relationship to a filtered reference, with retained selection inside a deadband. They do not define two physical movement directions. Their positive values limit the step per update while the target maps set the requested angle.

### Sensor calibration and throttle geometry

MAF transfer curves assign normalized readings to frequency. Zero-flow offsets are subtracted afterward, so raising an offset lowers the corrected reading. Channels A and B are software identities whose physical banks remain unassigned.

The MAP transfer is linear: `reported absolute pressure = gain * sensor voltage + offset`. Use the sensor's specified slope and offset together. Increasing the signed engineering offset raises the reading, while increasing the raw stored offset lowers it because the ECU subtracts that coefficient. These settings change pressure interpretation, not the boost request.

Throttle opening-area curves describe modeled geometry at each angle coordinate. They do not directly command the motor. Angle ceilings permit or restrict a command through particular boundaries; they do not request an opening by themselves. Body numbering is software order, not a confirmed left/right bank assignment.

### Protection and cooling

The main speed ceiling, speed-based fuel cut and fuel resume are separate controls. Preserve a cut/resume switching gap. The RPM entry margin is also separate: it adds to the paired minimum RPM for entry and does not change the already-set release boundary. The RPM-by-speed curve is a secondary engine-speed limit.

Coolant, charge-air, oil and driver-thermal controls supply torque-model candidates. The coolant schedule can restrict cold operation as well as hot operation. Other candidates can mask a relaxed limit. Zero is not a universal disable value; in the driver-thermal ceiling it means a zero ceiling, while results at the source no-limit threshold near 3276.7 select that branch's inactive candidate.

The oil blend uses model capacity below Start and reaches the oil-protection curve at Full when enabled. Its stored endpoint gap must be at least 1.0 native unit; a smaller or reversed gap selects the curve directly. Under the inherited temperature view that difference corresponds to 1 Celsius degree or 1.8 Fahrenheit degrees. The physical oil-state temperature conversion remains qualified. The curve can exceed its model reference at some speeds, so its name alone does not prove it always reduces torque.

Cooling fan stages and speed bands choose discrete duty cells. Lower turn-on temperatures engage a stage sooner, while positive hysteresis retains it farther into falling temperature. The duty tables request a percentage; other demands can override it, and the percentage is not measured fan RPM. Normal and alternate are software states rather than confirmed named driving modes.

## Map reference

The descriptions below match the text used in the tuner XML. Names refer to the tuner set; the separate function guide records their source names and technical references.

## Fuel Targets

### AFR Target - Normal

Sets the normal mixture request by engine speed and load. In the AFR view, lower values request a richer mixture and higher values request a leaner mixture. The equivalence-ratio view runs in the opposite direction: a larger ratio requests richer operation. The AFR display uses a fixed 14.7 reference. Selection, corrections and limits can change the final request, so measured AFR may differ.

### AFR Target - Safe Mode

Sets the alternate mixture request when the ECU selects the schedule labelled safe mode. Lower AFR values request richer operation; higher AFR values request leaner operation. Equivalence ratio runs in the opposite direction. This map uses the same fixed 14.7 AFR reference as the normal target. Its exact entry conditions remain unconfirmed, so do not assume a particular fault will select it.

### Fuel Rail Pressure Target - Normal

Requests fuel-rail pressure by fuel quantity and engine speed. Raising a cell requests more pressure; lowering it requests less. Low-speed schedules, operating modes and pressure limits can change the selected target. The pump controller determines how to pursue that target, so this table does not directly set pump angle or guarantee measured pressure. Pressure is available in MPa, bar and psi.

### Fuel Rail Pressure Target - Low RPM

Provides a pressure-request schedule for the low-speed control family. Raising a cell increases this schedule's request; lowering it reduces the request. Coolant and operating conditions determine when it contributes. The selected target still passes through pressure limits and the pump controller. A change here can have no visible effect while another schedule is selected.

### Fuel Rail Pressure Target - Lower Limit

Sets a minimum pressure-model value on the paths that apply this floor. Raising it raises that floor; lowering it permits a lower value. Some operating paths bypass the floor and later limits can reduce the result. It is not an unconditional minimum measured rail pressure.

## Boost Control

### Desired Boost

Supplies an engine-speed-based pressure request to the primary desired-boost channel. Raising a cell raises this curve's request; lowering it lowers the request. Other schedules, blending and downstream limits determine how much reaches the controller. Review the paired request curve too. The gauge display subtracts a fixed one bar; that reference is not live atmospheric pressure. Confirm displayed requests against logs before treating an edit as an equal change in measured boost.

### Desired Boost - Paired Request

Supplies the pressure request for the channel paired with Desired Boost. Raising or lowering a cell changes this channel's request in the same numerical direction. Its own selection and blending determine its contribution. Changing the primary curve does not automatically change this one. The paired channel's physical role remains unconfirmed. Its gauge display uses the same fixed one-bar reference.

### Wastegate Air-Temperature Correction

Adds a temperature-related contribution to wastegate demand after multiplication by an operating-point factor. With a positive factor, raising a cell adds more of this contribution and lowering it adds less. It does not change the desired-boost target. The millimetre display and the physical opening/closing direction remain unconfirmed, so a larger number must not be read as a guaranteed increase or decrease in boost.

### Wastegate Transient Feedback Correction

Changes a feedback lookup used during boost-control transients. Raising a cell increases the value returned at that boost error and engine speed; lowering it decreases the value. The ECU combines the result with its other feedback, feed-forward and protection terms. This is a response correction, not the boost-pressure target. The duty display is inherited; a larger cell is not a guaranteed increase in actuator duty or boost.

### Wastegate Integral Feedback Correction

Changes the lookup in the source-labelled integral boost-feedback path. Raising a cell increases this lookup's value; lowering it decreases the value. Accumulation, gain selection and other feedback terms determine the resulting correction. This table is separate from the Integral Contribution Multiplier and does not set desired boost. The duty display is inherited; the lookup value is not a direct actuator command.

### Wastegate Proportional Feedback Correction

Changes the lookup in the proportional boost-feedback path at each boost error and engine speed. Raising a cell raises the returned value; lowering it lowers that value. Multiplication and the other control terms determine the resulting contribution. It changes feedback response rather than the requested pressure. The inherited duty display does not make the cell a direct wastegate-duty command.

### Boost Integral Contribution Multiplier

Scales the selected integral contribution after the ECU chooses its retained state. A larger positive multiplier makes that signed contribution stronger; a smaller one makes it weaker. A negative state becomes more negative when multiplied by a larger positive value. Separate controls govern how the state accumulates. Mode selection, the feedback enable condition and later limits can override the effect.

### Boost Derivative Contribution Gain

Scales a signed change term in boost feedback. On the map-selected path, a larger positive value strengthens that contribution and a smaller value weakens it. The contribution can be positive or negative, so more gain does not simply mean more boost. Some modes select another coefficient, and the combined feedback correction can be switched off. This is separate from Desired Boost.

### Boost Correction - Upper Load Reference

Sets the upper reference paired with the Lower Load Reference. Raising it widens the reference range and generally reduces the calculated position within that range when the other inputs stay fixed. Lowering it narrows the range and can increase that position. The atmospheric multiplier then determines the direction of the resulting correction. Keep the upper reference above the lower one; a very small gap makes the calculation sensitive.

### Boost Correction - Lower Load Reference

Sets the lower reference paired with the Upper Load Reference. Inputs at or below it give zero position for this atmospheric-correction stage. Raising it moves that zero-contribution boundary upward and narrows the reference gap. Lowering it moves the boundary downward and widens the gap. Its effect depends on the upper reference and atmospheric multiplier; it is not a direct boost-pressure limit.

### Boost Correction - Atmospheric Multiplier

Controls the atmospheric adjustment applied through the upper/lower load-reference calculation. A value of 1 leaves this stage neutral. Values above 1 increase its factor as the calculated position rises; values below 1 reduce it. The curve has no effect at zero position. Later scaling and an output cap can limit the change. This is an internal correction factor, not a pressure target.

## Ignition Timing

### Ignition Timing - Base

Sets the normal starting value for the ignition-timing calculation by engine speed and load. Raising a cell raises the base timing value; lowering it lowers that value. The ECU can select an alternate base schedule and then apply corrections, knock response and timing limits. An edit therefore does not guarantee an equal change in final spark timing.

### Ignition Timing - Maximum Limit

Sets a maximum timing value by engine speed and load. Lowering an active limit restricts the timing calculation; raising it relaxes that limit while other timing controls can still be more restrictive. The source uses 127 as a no-limit value, not a request for 127 degrees of advance. Review the load axis as normalized engine load, not pedal position.

### Alternate Timing Limit - Mode 1

Sets the alternate limit value when the ECU selects Mode 1; Mode 4 supplies the other alternate schedule. Raising a cell raises this calculated limit and lowering it lowers the limit. Its full connection to final ignition timing remains unconfirmed. Do not interpret it as a universal knock-retard allowance or assume it is always active.

### Minimum Timing Contribution - Mode 2

Sets a contribution to the normal minimum-timing calculation. Some states select it directly; others use it below the coolant-blend range and blend toward Mode 3 as coolant rises. When this lower bound is active, raising it prevents a lower timing value and lowering it permits a lower value. Other timing inputs and limits still affect the result.

### Minimum Timing Contribution - Mode 3

Sets the warm-side contribution to the minimum-timing calculation when coolant blending is enabled. Its contribution grows between the coolant blend start and end. Raising an active lower bound prevents a lower timing value; lowering it permits a lower value. States that select Mode 2 directly bypass the blend. Other timing inputs and limits still apply.

### Alternate Timing Limit - Mode 4

Sets the alternate limit value when the ECU selects Mode 4; Mode 1 supplies the other alternate schedule. Raising a cell raises this calculated limit and lowering it lowers the limit. Its full connection to final ignition timing remains unconfirmed. Do not interpret it as a universal knock-retard allowance or assume it is always active.

### Minimum Timing Coolant Blend - Start

Sets where the coolant-based transition from Mode 2 toward Mode 3 begins. Raising the start delays the transition; lowering it begins sooner. The timing effect depends on the difference between those maps. The ECU uses a minimum blend span of one Celsius degree, and some states bypass the blend by selecting Mode 2 directly.

### Minimum Timing Coolant Blend - End

Sets the warm end of the coolant transition from Mode 2 toward Mode 3. Raising the end spreads the transition over a wider temperature range; lowering it brings in the full Mode 3 contribution sooner. The ECU enforces a minimum span of one Celsius degree. The timing effect depends on the two maps and whether blending is selected.

## Driver Demand and Torque

### Driver Demand - Drive Force Target 1

Converts accelerator-pedal position and vehicle speed into a requested drive force. Raising a cell asks for more drive force at that operating point; lowering it asks for less. The change affects pedal demand, not guaranteed delivered torque. Other demand schedules, traction functions, torque limits and protection can change the final request. The force views retain the source N and lbf conversions.

### Torque Model - Load and RPM

Sets a torque-model value for each engine-speed and normalized-load region. Raising a cell raises the modeled value where it contributes; lowering it lowers that value. This changes the ECU's model rather than directly commanding more or less engine torque. The oil-protection calculation also uses a reference from this model. Other calculations and torque limits determine the final request. The torque and load displays retain the source conventions.

### Torque Model - Coolant Multiplier

Multiplies a load-to-torque model result according to coolant temperature and engine speed. A larger positive multiplier raises that modeled value; a smaller one lowers it. A value of 1 leaves this multiplication neutral. Separate rate limits control how quickly the selected multiplier is applied. Changing a torque estimate can affect arbitration, so a larger value does not guarantee more delivered torque.

### Throttle Angle Ceiling - Body 1

Sets an engine-speed-based angle ceiling for software throttle body 1. Raising a point permits a higher angle through this boundary; lowering it applies a lower ceiling. It does not itself request throttle opening, and another limiter or operating mode can determine the final command. The angle view is inherited from the source definition; body numbering is software order.

### Throttle Angle Ceiling - Body 2

Sets an engine-speed-based angle ceiling for software throttle body 2. Raising a point permits a higher angle through this boundary; lowering it applies a lower ceiling. It does not itself request throttle opening, and another limiter or operating mode can determine the final command. The angle view is inherited from the source definition; body numbering is software order.

### Additional Throttle Angle Ceiling 1

Sets an additional angle boundary in throttle control. Raising it relaxes this particular ceiling; lowering it tightens the ceiling. It is not a direct opening request, and the speed-based ceilings, other limits and operating modes can still determine the final command. The exact relationship between the inherited angle coordinate and physical plate angle remains qualified.

## Cam Timing

### Intake Cam Target - Selector 03

Sets the intake cam-angle target for ECU selector state 3, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target - Selector 04

Sets the intake cam-angle target for ECU selector state 4, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target - Selector 05

Sets the intake cam-angle target for ECU selector state 5, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target - Selector 06

Sets the intake cam-angle target for ECU selector state 6, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target - Selector 07

Sets the intake cam-angle target for ECU selector state 7, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target - Selector 08

Sets the intake cam-angle target for ECU selector state 8, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target - Selector 09

Sets the intake cam-angle target for ECU selector state 9, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target - Selector 10

Sets the intake cam-angle target for ECU selector state 10, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 03

Sets the exhaust cam-angle target for ECU selector state 3, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 04

Sets the exhaust cam-angle target for ECU selector state 4, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 05

Sets the exhaust cam-angle target for ECU selector state 5, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 06

Sets the exhaust cam-angle target for ECU selector state 6, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 07

Sets the exhaust cam-angle target for ECU selector state 7, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 08

Sets the exhaust cam-angle target for ECU selector state 8, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 09

Sets the exhaust cam-angle target for ECU selector state 9, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Exhaust Cam Target - Selector 10

Sets the exhaust cam-angle target for ECU selector state 10, by engine speed and normalized load. Raising a cell raises this schedule's numerical target; lowering it lowers the target. The ECU can blend selected schedules and apply fallback logic. The state number is not a confirmed gear or driving mode. The angle convention is inherited, so a numerical increase has not been identified as physical advance or retard.

### Intake Cam Target Step Limit - Branch A

Limits the step toward the requested intake cam target on the rate branch normally selected when the request is at or above a filtered reference. With positive values, a larger cell permits a larger step and a smaller cell slows the step. Selection can be retained inside a deadband, and the command direction is calculated separately. This branch is not a confirmed advance/retard direction. The display is per control update; its time period is unverified.

### Intake Cam Target Step Limit - Branch B

Limits the step toward the requested intake cam target on the rate branch normally selected when the request is below a filtered reference. With positive values, a larger cell permits a larger step and a smaller cell slows the step. Selection can be retained inside a deadband, and the command direction is calculated separately. This branch is not a confirmed advance/retard direction. The display is per control update; its time period is unverified.

### Exhaust Cam Target Step Limit - Branch A

Limits the step toward the requested exhaust cam target on the rate branch normally selected when the request is at or above a filtered reference. With positive values, a larger cell permits a larger step and a smaller cell slows the step. Selection can be retained inside a deadband, and the command direction is calculated separately. This branch is not a confirmed advance/retard direction. The display is per control update; its time period is unverified.

### Exhaust Cam Target Step Limit - Branch B

Limits the step toward the requested exhaust cam target on the rate branch normally selected when the request is below a filtered reference. With positive values, a larger cell permits a larger step and a smaller cell slows the step. Selection can be retained inside a deadband, and the command direction is calculated separately. This branch is not a confirmed advance/retard direction. The display is per control update; its time period is unverified.

## Sensors and Hardware

### MAF Calibration - Channel A

Converts MAF sensor frequency into the normalized reading for software channel A. Raising a point reports a larger reading at that frequency; lowering it reports a smaller reading. Zero-flow correction, fault handling and later limits can change the final published value. The percent view is a normalized sensor scale, not grams per second or throttle opening. Channel A has not been assigned to a physical engine bank.

### MAF Calibration - Channel B

Converts MAF sensor frequency into the normalized reading for software channel B. Raising a point reports a larger reading at that frequency; lowering it reports a smaller reading. Zero-flow correction, fault handling and later limits can change the final published value. The percent view is a normalized sensor scale, not grams per second or throttle opening. Channel B has not been assigned to a physical engine bank.

### MAF Zero-Flow Offset - Channel A

Sets the offset subtracted from software MAF channel A before the channels are combined. Raising this offset subtracts more and lowers the corrected reading; lowering it subtracts less and raises the reading. The percent view is a fraction of the stored sensor scale. Fault substitution can change how the corrected channel is used. Channel A is a software identity, not a confirmed physical bank.

### MAF Zero-Flow Offset - Channel B

Sets the offset subtracted from software MAF channel B before the channels are combined. Raising this offset subtracts more and lowers the corrected reading; lowering it subtracts less and raises the reading. The percent view is a fraction of the stored sensor scale. Fault substitution can change how the corrected channel is used. Channel B is a software identity, not a confirmed physical bank.

### MAP Sensor Pressure Gain

Sets the slope of the linear manifold absolute-pressure sensor conversion. Raising the gain increases reported pressure at a fixed positive sensor voltage; lowering it decreases the reading. Calibrate it together with the pressure offset using the sensor's specified transfer. This setting changes how the ECU reads pressure; it does not set desired boost. The engineering views show pressure per volt.

### MAP Sensor Pressure Offset

Sets the constant pressure contribution added in the linear MAP conversion. In the signed pressure view, a higher value, including a less-negative value, raises the reported pressure; a lower value lowers it. The raw stored coefficient runs in the opposite direction because the ECU subtracts it. Calibrate the offset with the gain. This is an absolute-pressure sensor calibration, not a boost target.

### Throttle Model - Body 1 Opening Area

Converts the angle coordinate into modeled effective opening area for software throttle body 1. Raising a point tells the air model that more area is available at that coordinate; lowering it reports less area. This changes modeled geometry rather than directly setting the motor command. Review it against the actual throttle hardware. Software body numbering has not been confirmed as a physical left/right bank.

### Throttle Model - Body 2 Opening Area

Converts the angle coordinate into modeled effective opening area for software throttle body 2. Raising a point tells the air model that more area is available at that coordinate; lowering it reports less area. This changes modeled geometry rather than directly setting the motor command. Review it against the actual throttle hardware. Software body numbering has not been confirmed as a physical left/right bank.

## Protection and Limits

### Vehicle Speed - Main Ceiling

Sets the main ceiling in the vehicle-speed request and control calculations. Raising it permits a higher ceiling in those calculations; lowering it applies a lower ceiling. Separate speed-based fuel-cut and fuel-resume settings can still intervene. Review all three together; changing this value alone does not establish the final vehicle-speed limit.

### Vehicle Speed - Fuel Cut

Sets the filtered vehicle speed at which this control requests an all-cylinder fuel cut. Raising it moves this speed threshold higher; lowering it moves the threshold lower. The fuel-resume value sets the lower release threshold, and separate RPM eligibility conditions also apply. Keep the cut value above the resume value to retain a switching gap.

### Vehicle Speed - Fuel Resume

Sets the filtered vehicle speed below which this speed-based fuel cut releases. Raising it resumes fuel at a higher falling speed; lowering it retains the cut down to a lower speed. Keep it below the Fuel Cut threshold to preserve a switching gap. This setting does not override the separate engine-speed or other cut conditions.

### Engine Speed Limit by Vehicle Speed

Sets a secondary RPM limit according to filtered vehicle speed. Raising a point permits more RPM through this limit; lowering it sets a lower limit in that speed region. Values between speed points are interpolated. Other rev limits and operating conditions can intervene independently, so this curve does not by itself establish the final engine-speed limit.

### Speed Fuel-Cut RPM Entry Margin

Sets the extra RPM needed to enter the speed-cut eligibility state when it was previously clear. Entry requires the paired minimum RPM plus this margin. Once eligible, the state stays set down to the paired minimum RPM. Raising a positive margin increases re-entry RPM without changing that release boundary; zero removes the gap. This is an RPM difference, not a delay or a standalone rev limit.

### Torque Protection - Coolant and RPM

Sets a coolant- and RPM-dependent torque-model ceiling. Lowering an active cell tightens this protection branch; raising it relaxes that branch. The charge-air correction is added before the branch competes with the fail-safe ceiling. The stock schedule also restricts cold operation, so this is not only an overheat limit. Other torque limits can mask a change. The torque display is a model convention, not a measurement of delivered torque.

### Torque Protection - Oil-Temperature Curve

Sets the RPM-based curve used by the enabled oil-related protection path. The start/full thresholds blend from model-based capacity toward this curve. Raising a point raises the path's output where it contributes; lowering it lowers that output. The curve can exceed the model reference at some speeds, so do not assume it always reduces torque. Other limits can still win. Torque and oil-temperature displays retain the source conventions.

### Oil-Temperature Torque Blend - Start

Sets the lower endpoint of the oil-related torque blend. Below it, the enabled path uses model-based capacity. Above it, the path blends toward the oil-protection curve. When that curve is lower, raising this threshold delays the reduction and lowering it starts the reduction sooner. Keep the Full threshold at least one native unit higher; a smaller or reversed span selects the protection curve directly. The temperature display follows the source convention.

### Oil-Temperature Torque Blend - Full

Sets where the enabled oil-related path reaches its protection curve. When that curve is lower than model-based capacity, raising this threshold delays the full reduction and lowering it reaches the curve sooner. Keep it at least one native unit above Start; a smaller or reversed span selects the protection curve directly. Raising either endpoint can reduce protection in the affected interval. Temperature units retain the source convention.

### Torque Protection - Charge-Air Correction

Adds a temperature-dependent correction to the coolant/RPM torque ceiling. Zero leaves that ceiling unchanged. Positive values raise it and negative values lower it. A change affects the selected protection ceiling only while this combined branch is the more restrictive candidate. Another limit can mask it. The temperature and torque views retain the source model conventions.

### Torque Protection - Driver Thermal Ceiling

Supplies an RPM-based torque-model ceiling when its admission condition is active. Lowering a cell tightens this candidate; raising it relaxes the candidate. Zero means a zero ceiling, not a disabled monitor. Results at or above the source threshold near 3276.7 select the no-limit candidate in this branch. Other limits and recovery state can mask or delay a change. The precise admission conditions remain unconfirmed.

## Cooling Fans

### Cooling Fan - Stage Turn-On Temperatures

Sets the turn-on temperature for each coolant fan stage in two software states. Lowering a threshold engages that stage sooner; raising it delays engagement. The matching temperature hysteresis controls when an active stage releases. Stages select duty-table columns; they are discrete steps, not an interpolated curve. The alternate state is not a confirmed named driving mode, and other fan demands can override this request.

### Cooling Fan - Temperature Hysteresis

Sets how far coolant must fall below a stage's turn-on temperature before that active stage releases. A larger positive value keeps the stage active longer as temperature falls; a smaller value narrows the switching gap. It does not change initial turn-on. The Fahrenheit view is a temperature difference and has no 32-degree offset. Other fan demands can keep the final command higher.

### Cooling Fan Duty - Normal State

Sets requested fan duty for each coolant stage and vehicle-speed band in the normal software state. Raising an active cell requests more duty; lowering it requests less. The percent view displays the request fraction. Stages and bands select discrete cells without interpolation. Full-demand overrides, low-stage inhibition and other fan demands can replace this request. Requested duty is not measured fan RPM.

### Cooling Fan Duty - Alternate State

Sets requested fan duty for each coolant stage and vehicle-speed band in the alternate software state. Raising an active cell requests more duty; lowering it requests less. Cells are selected by stage and speed band without interpolation. Internal conditions select this table; they have not been assigned to a named driving mode. Full-demand overrides and later fan-demand selection can mask an edit.

### Cooling Fan - Speed-Band Thresholds

Sets the three speed boundaries that select four duty-table rows. Lowering a threshold selects the next band at a lower rising speed; raising it keeps the previous band selected longer. The resulting duty change depends on the affected rows. Separate hysteresis retains an active band as speed falls. These are selection thresholds, not direct fan-duty values.

### Cooling Fan - Speed-Band Hysteresis

Sets how far speed may fall below a band-entry threshold before the active band releases. A larger positive value retains the higher band farther into falling speed; a smaller value narrows that gap. Initial entry on rising speed is unchanged. The fan-duty effect depends on the selected rows, and other fan demands can override it.

## Further technical controls

The full technical set retains controls whose names, activation or physical meaning need more work. The tuner set omits the unresolved base wastegate/duty/atmospheric maps, the source-labelled WOT AFR threshold, four MBT/SCV references and the source-labelled intake-air sensor conversion. Their existing definitions and evidence are preserved for further investigation.
