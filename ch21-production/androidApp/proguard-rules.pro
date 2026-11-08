# ch21-production/androidApp/proguard-rules.pro
# kotlinx.serialization ships its R8 rules inside its jar
# (META-INF/com.android.tools/r8/), so nothing is needed for Session.
# Add a narrow -keep rule only when R8 removes something that is reached
# by reflection alone; never -keep class ** (it switches R8 off in practice).
