oti
===

An indexing tool for nexsons.

Install
-----

####Clone the repo:

First, clone a copy of the oti repo on your local machine.

```
git clone https://github.com/OpenTreeOfLife/oti.git
```

####Install dependencies

OTI requires the jade and ot-base repositories to be installed into the local maven directory. Run the following commands after you clone to download the dependencies from git and install them into your local maven repository cache. Of course, this requires maven (version 3).

Running this script will clone the ot-base and jade repos into the parent directory of the oti repo. To place them elsewhere, install them manually. See directions at https://github.com/FePhyFoFum/jade and https://github.com/OpenTreeOfLife/ot-base.

```
cd oti
sh mvn_install_dependencies.sh
```

OTI uses the jade and ot-base classes for many things. The source code for these dependencies is reasonably well-documented. You can refer to the class files in the jade and ot-base directories for more information. If you use Eclipse (with the m2eclipse plugin), just import the ot-base and jade repos as maven projects to browse their packages and classes.