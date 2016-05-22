# Project Atlas

## Development Mode

### Run application:

```
lein figwheel
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

Warning: figwheel only knows how to serve static resources, whereas we use HTML
history API to push changes to the address bar without refreshing the page.
Hence, figwheel will only work if you start from the root.

If you want to interact with the server, start it with

```
lein run
```

You can then connect on [http://localhost:2442](http://localhost:2442) for the
full experience.

To develop on the backend, start the development server through the commented
code in [core.clj](src/clj/atlas/core.clj).

## Production Build

```
lein clean
lein cljsbuild once prod
```
