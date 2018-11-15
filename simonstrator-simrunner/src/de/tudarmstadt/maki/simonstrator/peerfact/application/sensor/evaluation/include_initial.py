__author__ = 'Michael'

# includes the initial results into the main result folder. this is only a workaround!
# reason: i wanted to include the initial metrics into the plots with the results, but the aggregation script looks only into the results folder...
# so, this script copies the initial metrics into the result folder. the subfolder name has to be configured for each execution independently

import os
import globals
import shutil

# returns the name of all sub directories (not the path)
def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir)
            if os.path.isdir(os.path.join(dir, name))]

def copyDirectory(src, dest):
    try:
        shutil.copytree(src, dest)
    # Directories are the same
    except shutil.Error as e:
        print('Directory not copied. Error: %s' % e)
    # Any error saying that the directory doesn't exist
    except OSError as e:
        print('Directory not copied. Error: %s' % e)

sub_dirs = get_immediate_subdirectories(globals.ROOT_FOLDER)
i = 1
for seedDirectoryName in sub_dirs:

    print("Current directory (" + str(i) + "/" + str(len(sub_dirs)) + "): " + str(os.path.join(globals.ROOT_FOLDER, seedDirectoryName)))
    i += 1

    initialPath = os.path.join(globals.ROOT_FOLDER, seedDirectoryName, "inital")
    seedResultPath = os.path.join(globals.ROOT_FOLDER, seedDirectoryName, "result", "a=1.0_overlayMetric=UNSPEC_overlayConstantType=UNSPEC_k=UNSPEC_overlayEnabled=INITIAL")

    # copy initial folder to corresponding result folder
    copyDirectory(initialPath, seedResultPath)

