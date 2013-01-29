//
//  PlayerViewController.m
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#define kIdMin 1
#define kIdMax 1000
#import "PlayerViewController.h"
//#import <SDWebImage/UIImageView+WebCache.h>
#import "UIImageView+WebCache.h"
//#import "SDWebImageDownloader.h"
//#import "SDImageCache.h"
#import "ASIHTTPRequest.h"
#import "ASIDownloadCache.h"
//#import "ASINetworkQueue.h"

@interface PlayerViewController ()
//- (void)imageFetchComplete:(ASIHTTPRequest *)request;
//- (void)imageFetchFailed:(ASIHTTPRequest *)request;
@end

@implementation PlayerViewController

//@synthesize panoId;
@synthesize faceNum;
@synthesize hotspots;
@synthesize faceSL;
@synthesize faceSR;
@synthesize faceSF;
@synthesize faceSB;
@synthesize faceSD;
@synthesize faceSU;

@synthesize imageProgressIndicator;

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    plView = (PLView *)self.view;
    plView.delegate = self;
    
    loading = [[[UILabel alloc] initWithFrame:CGRectZero] autorelease];
    loading.text = @"(1/6)素材加载中...";
    loading.backgroundColor = [UIColor clearColor];
    loading.font = [UIFont fontWithName:@"Arial" size:12];
    loading.textAlignment = UITextAlignmentCenter;
    //loading.font
    [self.view addSubview:loading];
    
    CGSize result = [[UIScreen mainScreen] bounds].size;
    int height = result.height;
    int width = result.width;
    int progressWdith = width/2;
    int x = (width - 110)/2;
    int y = (height/2)-100;
    NSLog(@"X-Y=%d-%d", x, y);
    [loading setFrame:CGRectMake(x,y,110,20)];
    
    imageProgressIndicator = [[[UIProgressView alloc] initWithFrame:CGRectZero] autorelease];
    [self.view addSubview:imageProgressIndicator];
    y = y+20;
    x = (width - progressWdith)/2;
    [imageProgressIndicator setFrame:CGRectMake(x,y,progressWdith,30)];
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(panoPlayerNotificationHandler:) name:@"panoId" object:nil];
}

- (void)panoPlayerNotificationHandler:(NSNotification*)notification  
{  
    NSString *panoId = [[notification userInfo] objectForKey:@"panoId"];
    /*[NSThread detachNewThreadSelect:@selector(startPlayer)
                           toTarget:self
                         withObject:nil];
    */
    [NSThread detachNewThreadSelector:@selector(startPlayer:) toTarget:self withObject:panoId];
    //[self startPlayer:panoId];
} 

-(void)startPlayer:(NSString *)panoId{

    faceNum = 0;
    hotspots = [[NSMutableArray alloc] init];

    Boolean flag = false;
    
    
    if(panoId != nil){
        NSString *panoInfoUrl = [NSString stringWithFormat:@"http://beta1.yiluhao.com/ajax/m/pv/id/%@", panoId];
        //NSLog(@"panoInfoUrl=%@", panoInfoUrl);
        
        NSString *responseData = [self getPanoInfoFromUrl:panoInfoUrl];
        if(responseData !=nil){
            NSDictionary *resultsDictionary = [responseData objectFromJSONString];
            NSArray *hotspotDct = [resultsDictionary objectForKey:@"hotspots"];
            //NSLog(@"res=%@", [ret objectForKey:@"panos"]);
            for(int i=0; i<hotspotDct.count; i++){
                NSDictionary  *tmp = [hotspotDct objectAtIndex:i];
                
                NSString *hotspotId = [tmp objectForKey:@"id"];
                NSString *link_scene_id = [tmp objectForKey:@"link_scene_id"];
                NSString *tilt = [tmp objectForKey:@"tilt"];
                NSString *pan = [tmp objectForKey:@"pan"];
                [self addHotspot:hotspotId linkSceneId:link_scene_id tilt:tilt pan:pan];
                //NSLog(@"id=%@", hotspotId);
                //NSLog(@"link_scene_id=%@", link_scene_id);
            }
            
            
            ASIDownloadCache *cache = [[ASIDownloadCache alloc] init];
            NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
            [cache setStoragePath:[cachePath stringByAppendingPathComponent:@"resource"]];
            
            //UIImage *img;
            ASIHTTPRequest *request;

            NSDictionary *pano = [resultsDictionary objectForKey:@"pano"];
            NSString *s_f = [pano objectForKey:@"s_f"];
            request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:s_f]];
            [request setDownloadCache:cache];
            [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
            [request setSecondsToCache:60*60*24*30*10]; //30
            [request setDownloadProgressDelegate:imageProgressIndicator];
            [request setUserInfo:[NSDictionary dictionaryWithObject:@"s_f" forKey:@"face"]];
            Boolean cached = [cache isCachedDataCurrentForRequest:request];
            if(cached){
                NSLog(@"cached2");
            }
            [request startSynchronous];
            NSError *error = [request error];
            if (!error) {
                faceSF = [UIImage imageWithData:[request responseData]];
                //NSLog(@"response%@", responseData);
            }
            else {
                [self getWrong:@"获取素材失败"];
            }

            //loading.text = @"(2/6)素材加载中...";
            [loading setText:@"(2/6)素材加载中..."];
            //[self.view addSubview:loading];
            NSString *s_r = [pano objectForKey:@"s_r"];
            request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:s_r]];
            [request setDownloadCache:cache];
            [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
            [request setSecondsToCache:60*60*24*30*10]; //30
            [imageProgressIndicator setProgress:0];
            [request setDownloadProgressDelegate:imageProgressIndicator];
            [request setUserInfo:[NSDictionary dictionaryWithObject:@"s_r" forKey:@"face"]];
            
            [request startSynchronous];
            error = [request error];
            if (!error) {
                faceSR = [UIImage imageWithData:[request responseData]];
                //NSLog(@"response%@", responseData);
            }
            else {
                [self getWrong:@"获取素材失败"];
            }
            
            cached = [cache isCachedDataCurrentForRequest:request];
            if(cached){
                NSLog(@"cached3");
            }
            
            loading.text = @"(3/6)素材加载中...";
            loading.hidden = YES;
            NSString *s_b = [pano objectForKey:@"s_b"];
            request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:s_b]];
            [request setDownloadCache:cache];
            [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
            [request setSecondsToCache:60*60*24*30*10]; //30
            [imageProgressIndicator setProgress:0];
            [request setDownloadProgressDelegate:imageProgressIndicator];
            [request setUserInfo:[NSDictionary dictionaryWithObject:@"s_b" forKey:@"face"]];
            
            [request startSynchronous];
            error = [request error];
            if (!error) {
                faceSB = [UIImage imageWithData:[request responseData]];
                //NSLog(@"response%@", responseData);
            }
            else {
                [self getWrong:@"获取素材失败"];
            }

            cached = [cache isCachedDataCurrentForRequest:request];
            if(cached){
                NSLog(@"cached4");
            }
            
            loading.text = @"(4/6)素材加载中...";
            NSString *s_l = [pano objectForKey:@"s_l"];
            request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:s_l]];
            [request setDownloadCache:cache];
            [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
            [request setSecondsToCache:60*60*24*30*10]; //30
            [request setUserInfo:[NSDictionary dictionaryWithObject:@"s_l" forKey:@"face"]];
            
            [request startSynchronous];
            error = [request error];
            if (!error) {
                faceSL = [UIImage imageWithData:[request responseData]];
                //NSLog(@"response%@", responseData);
            }
            else {
                [self getWrong:@"获取素材失败"];
            }

            cached = [cache isCachedDataCurrentForRequest:request];
            if(cached){
                NSLog(@"cached5");
            }
            
            loading.text = @"(5/6)素材加载中...";
            NSString *s_u = [pano objectForKey:@"s_u"];
            request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:s_u]];
            [request setDownloadCache:cache];
            [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
            [request setSecondsToCache:60*60*24*30*10]; //30
            [request setUserInfo:[NSDictionary dictionaryWithObject:@"s_u" forKey:@"face"]];
            
            [request startSynchronous];
            error = [request error];
            if (!error) {
                faceSU = [UIImage imageWithData:[request responseData]];
            }
            else {
                [self getWrong:@"获取素材失败"];
            }
            cached = [cache isCachedDataCurrentForRequest:request];
            if(cached){
                NSLog(@"cached6");
            }
            
            loading.text = @"(6/6)素材加载中...";
            NSString *s_d = [pano objectForKey:@"s_d"];
            request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:s_d]];
            [request setDownloadCache:cache];
            [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
            [request setSecondsToCache:60*60*24*30*10]; //30
            [request setUserInfo:[NSDictionary dictionaryWithObject:@"s_d" forKey:@"face"]];
            
            [request startSynchronous];
            error = [request error];
            if (!error) {
                faceSD = [UIImage imageWithData:[request responseData]];
            }
            else {
                [self getWrong:@"获取素材失败"];
            }
            
            cached = [cache isCachedDataCurrentForRequest:request];
            if(cached){
                NSLog(@"cached7");
            }
            [self displayPano];
            flag = true;

        }
    }
    if(!flag){
        [self getWrong:@"1加载数据出错"];
    }
    else {
        
    }
}

 - ( void )requestFinished:( ASIHTTPRequest *)request
 {
    NSString *face = [[request userInfo] objectForKey:@"face"];
	UIImage *img = [UIImage imageWithData:[request responseData]];
	if(img){
        [self initPlayer:face faceImage:img];
	}
    else{
        //[self getWrong:@"下载素材失败"];
        NSLog(@"face=No");
    }

}

 - ( void )requestFailed:( ASIHTTPRequest *)request
 {
    //NSString *face = [[request userInfo] objectForKey:@"face"];
    //NSLog(@"face=%@", face);
	if (!failed) {
		[self getWrong:@"2获取素材失败，请检查您的网络设置"];
		failed = YES;
	}
}

-(void)addHotspot:(NSString *)hotspotId linkSceneId:(NSString *)linkSceneId tilt:(NSString *)tilt pan:(NSString *)pan{
    
    NSMutableDictionary *hotspotBox = [[NSMutableDictionary alloc] init];
    [hotspotBox setValue:hotspotId forKey:@"hotspotId"];
    [hotspotBox setValue:linkSceneId forKey:@"linkSceneId"];
    [hotspotBox setValue:tilt forKey:@"tilt"];
    [hotspotBox setValue:pan forKey:@"pan"];
    
    [hotspots addObject:hotspotBox];
    //NSLog(@"hotspots%@", hotspots);
}
-(void)initPlayer:(NSString *)face faceImage:(UIImage *)faceImage{
    //[self addFace:path face:face];
    faceNum++;

    if([face isEqualToString:@"s_f"]){
        faceSF = faceImage;
        //NSLog(@"faceSF=%@", faceSF);
    }
    else if([face isEqualToString:@"s_l"]) {
        faceSL = faceImage;
        //NSLog(@"faceSF=%@", faceSL);
    }
    else if([face isEqualToString:@"s_r"]) {
        faceSR = faceImage;
        //NSLog(@"faceSR=%@", faceSR);
    }
    else if([face isEqualToString:@"s_b"]) {
        faceSB = faceImage;
        //NSLog(@"faceSB=%@", faceSB);
    }
    else if([face isEqualToString:@"s_u"]) {
        faceSU = faceImage;
        //NSLog(@"faceSU=%@", faceSU);
    }
    else if([face isEqualToString:@"s_d"]) {
        faceSD = faceImage;
        //NSLog(@"faceSD=%@", faceSD);
    }
    
    //return;
    if(faceNum > 5){
        
        [self displayPano];
    }
     
}

-(void)displayPano{
    
    NSObject<PLIPanorama> *panorama = nil;
    PLCubicPanorama *cubicPanorama = [PLCubicPanorama panorama];

    CGImageRef cgFaceSF = CGImageRetain(faceSF.CGImage);
    //[faceSF release], faceSF = nil;

    CGImageRef cgFaceSB = CGImageRetain(faceSB.CGImage);

    CGImageRef cgFaceSD = CGImageRetain(faceSD.CGImage);

    CGImageRef cgFaceSL = CGImageRetain(faceSL.CGImage);

	CGImageRef cgFaceSR = CGImageRetain(faceSR.CGImage);

    CGImageRef cgFaceSU = CGImageRetain(faceSU.CGImage);

    NSLog(@"wait time----");
    
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSF]] face:PLCubeFaceOrientationFront];
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSL]] face:PLCubeFaceOrientationLeft]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSR]] face:PLCubeFaceOrientationRight]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSB]] face:PLCubeFaceOrientationBack]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSU]] face:PLCubeFaceOrientationUp]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSD]] face:PLCubeFaceOrientationDown];
    panorama = cubicPanorama;
    for (int i=0; i<hotspots.count; i++) {
       
        NSMutableDictionary *hotspotDct = [hotspots objectAtIndex:i];
        PLTexture *hotspotTexture = [PLTexture textureWithImage:[PLImage imageWithPath:[[NSBundle mainBundle] pathForResource:@"hotspot" ofType:@"png"]]];
        NSString *hotspotId = [hotspotDct objectForKey:@"hotspotId"];
        NSString *tilt = [hotspotDct objectForKey:@"tilt"];
        NSString *pan = [hotspotDct objectForKey:@"pan"];
        
        PLHotspot *hotspot = [PLHotspot hotspotWithId:[hotspotId intValue] texture:hotspotTexture atv:[tilt floatValue] ath:[pan floatValue] width:0.08f height:0.08f];

        [panorama addHotspot:hotspot];
         
    }
    [plView setPanorama:panorama];
    imageProgressIndicator.hidden = YES;
    loading.hidden = YES;
}

- (void) getWrong:(NSString*)str{
    NSString *msg = [NSString stringWithFormat:@"%@", str];
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil message:msg delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil, nil];
    [alert show];
    [alert release];
}

-(NSString *)getPanoInfoFromUrl:(NSString *)url{
    if(url == nil){
        [self getWrong:@"3参数错误"];
        return @"";
    }
    NSString *responseData = [[NSString alloc] init];
    
    ASIDownloadCache *cache = [[ASIDownloadCache alloc] init];
    NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    [cache setStoragePath:[cachePath stringByAppendingPathComponent:@"resource"]];    
    
    
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    
    
    [request setDownloadCache:cache];
    
    [request setCacheStoragePolicy:ASIAskServerIfModifiedWhenStaleCachePolicy];
    [cache setShouldRespectCacheControlHeaders:NO];
    //[]
    [request setSecondsToCache:60*60*24*30*10]; //30
    
    [request startSynchronous];
    
    NSError *error = [request error];
    if (!error) {
        responseData = [request responseString];
        //NSLog(@"response%@", responseData);
    }
    else {
        [self getWrong:@"4获取数据失败"];
    }
    
    
    return responseData;
}


//Hotspot event
-(void)view:(UIView<PLIView> *)pView didClickHotspot:(PLHotspot *)hotspot screenPoint:(CGPoint)point scene3DPoint:(PLPosition)position
{
    /*UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Hotspot" message:[NSString stringWithFormat:@"You select the hotspot with ID %d", hotspot.identifier] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];*/
    NSString *clickHotspotId = [NSString stringWithFormat:@"%d", hotspot.identifier];
    NSString *panoId = nil;
    for (int i=0; i<hotspots.count; i++) {
        NSMutableDictionary *hotspotDct = [hotspots objectAtIndex:i];
        NSString *linkScence = [hotspotDct objectForKey:@"linkSceneId"];
        NSString *hotspotId = [hotspotDct objectForKey:@"hotspotId"];
        if([hotspotId isEqualToString:clickHotspotId]){
            panoId = linkScence;
        }
    }
    NSLog(@"panoID=%@", panoId);

    [self startPlayer:panoId];

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    //[cubicPanorama release], cubicPanorama=nil;
    [plView release], plView = nil;
}

-(void)dealloc
{
    [super dealloc];
}


-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

-(void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return YES;
}

@end
