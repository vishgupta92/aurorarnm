# Prerequisites #

If you wish to participate in the day-to-day Aurora development, you need an account on [PATH server](http://path.berkeley.edu) with group membership _topl_.
You must be affiliated with UC Berkeley or with the TOPL project.



# Before You Start #

1. Download and install Eclipse from http://www.eclipse.org

2. If do not have JRE 1.6 installed already, download and install JDK 6 from http://java.sun.com/javase/downloads/index.jsp






# Loading the Aurora Project into Eclipse #

1. Go to **File --> New --> Project...** - this brings up a wizard window

2. Select **CVS --> Projects from CVS**

3. In the _Checkout Project from CVS Repository_ screen choose **Create new repository location** radio button, and click **Next**

4. Fill in the form in the _Enter Repository Location Information_ screen as shown below, and click **Next**
  * Host name: _path.berkeley.edu_
  * Repository path: _/home/topl/cvsroot_
  * User/Password: your login and password on [PATH server](http://path.berkeley.edu)
  * Connection type: _extssh_
  * Use default port: _selected_
  * Save password: _checked_ (better to save than type it each time)

5. Select **Use existing module** radio button in the _Select Module_ screen, and from the file tree select **aurora --> devel**, then click **Next**

6. Fill in the form in the _Check Out As_ screen as shown below, and click **Next**
  * Checkout as project in the workspace: _selected_
  * Checkout with subfolders: _checked_
  * Project name: _aurora_ (instead of _devel_)

7. Make the selection in the _Select Tag_ screen following the steps below, and click **Finish**
  * Click **Refresh tags** button
  * Select **Branches --> TOPL3\_20081015**

Now the Aurora project is loaded into Eclipse.

**Remark:** if for some reason you fail to connect to the repository in step 4, check your firewall settings - sometimes, firewall is the hindrance.






# Final Adjustments #

Once the project is checked out, before running it you need to add necessary jars into your _Classpath_ and set up the appropriate compiler.
To do that, follow the steps below.

1. Go to **Project --> Properties** in the menu bar of Eclipse - this brings up the _Properties for aurora_ window

2. Go to **Java Build Path** in a tree, then select **Libraries** tab and click **Add External JARs...** button

3. Add all jars from `libGUI` subdirectory except `gnujaxp.jar`; from `libdbDerby` add `derby.jar` and `derbytools.jar`; and from `libGIS` add all jars except `commons-collections-3.1.jar`

4. In the same widow, select **Java Compiler** and make sure that the compiler compliance level is 6.0

5. Click **OK**

6. Go to **Window --> Preferences** in the menu bar of Eclipse - this brings up the _Preferences_ window

7. Go to **Java --> Installed JREs** and select **JRE 1.6** (If **JRE 1.6** is not in the list, add the one you have previously installed)

8. Click **OK**

9. Go to **Run --> Run**, and start the Java application with the main class `aurora.hwc.gui.MainPane` - this starts the Simulator

10. To start the Configurator select the `aurora.hwc.config.MainPane` class

11. To see the icons correctly, copy (do not move, just copy) the `icons` subdirectory into `bin` subdirectory

At this point you are ready to start working on the code.