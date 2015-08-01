Changes in v0.5.0:
- Added support for the Implementors plugin (if installed).
- Added new preference items for enabling use of the Implementors plugin.
- Added use of the selected search scope when finding callees.
- Added use of a progress monitor.

Changes in v0.4.2:
- Changed the way keyboard shortcuts are declared in plugin.xml
- Enabled overlay icons in the label provider for the search results (it is now possible to see
	that it is a constructor.)

Changes in v0.4.1:
- Fixed a couple of bugs:
    - NPE when find calls from a method with an anonymous class creation.
	- No indication of the active search scope unless for working sets.

Changes in v0.4.0:
- Added the possibility to change the search scope (when searching for references to method).

Changes in v0.3.1:
- Fixed a couple of bugs related to finding calls from inner classes and calls to super methods.

Changes in v0.3.0:
- Added "Focus On Selection" functionality which roots the call hierarchy on the selected method.
- Added a history drop down containing the most recently shown methods.

Changes in v0.2.0:
- The callee view (calls into) has been reworked for the following functionality:
	- call details list
	- sorting of search results by call location
- Context menu now works in the search results

Changes in v0.1.0:
- Added keyboard shortcuts to refresh (F5) and toggle between callers/callees (F9).
- Added a Call Details view.
- Added a new preferences page for setting layout, format and max call depth.
- Settings are persisted (caller/callee mode, width of details view).

Todos and other open issues:
- Pressing enter on the call details list doesn't work.
- Find a way to present multiple implementation classes when showing callees "across" an interface.