This project is a minimal sample of a block in order to demonstrate the functionality 
of the block deployer (simple-deploy).

When the development of the block deployer has stabilized, this block's content will become an archetype too.

In order to test it, move to ./cocoon/trunk/cocoon-plugins and run

mvn install

and then, movto to ./cocoon/trunk/cocoon-plugins/cocoon-deployer-plugin-demo and run

mvn cocoon:simple-deploy

from your console.

WARNING: The created webapp can't be deployed into a servlet container!
